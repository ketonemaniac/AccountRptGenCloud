package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Header;
import net.ketone.accrptgen.entity.Paragraph;
import net.ketone.accrptgen.entity.Section;
import net.ketone.accrptgen.entity.SectionElement;
import net.ketone.accrptgen.entity.Table;
import net.ketone.accrptgen.store.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.functions.DateDifFunc;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
// import org.springframework.util.StringUtils;

import java.io.*;
import java.util.*;

@Component
public class ParsingService {

    private static final Logger logger = LoggerFactory.getLogger(ParsingService.class);
    private static final String COPY_COLOR = "4F81BD";

    @Autowired
    private StorageService storageService;

    @Value("${xlsx.template.name}")
    private String templateName;

    /**
     * Gets template workbook and fetches Sheets into a map
     * @return
     */
    private Map<String, Sheet> initSheetMap(Workbook wb) throws IOException {
        Map<String, Sheet> sheetMap = new HashMap<>();
        Iterator<Sheet> iter = wb.sheetIterator();
        while(iter.hasNext()) {
            Sheet sheet = iter.next();
            // logger.info("sheet: " + sheet.getSheetName());
            sheetMap.put(sheet.getSheetName(), sheet);
        }
        return sheetMap;
    }

    public ByteArrayOutputStream preParse(InputStream excelFile) throws IOException {

        XSSFWorkbook templateWb = storageService.getTemplate(templateName);
        if(templateWb == null) {
            throw new IOException("Unable to get File " + templateName);
        }
        Map<String, Sheet> templateSheetMap = initSheetMap(templateWb);

        XSSFWorkbook workbook = new XSSFWorkbook(excelFile);
        Map<String, Sheet> inputSheetMap = initSheetMap(workbook);

        for(Sheet sheet : inputSheetMap.values()) {
            Sheet templateSheet = templateSheetMap.get(sheet.getSheetName());
            if(templateSheet == null) continue;
            logger.info("parsing sheet: " + sheet.getSheetName());
            int count = 0;
            for (int r = 0; r < sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Iterator<Cell> cellIter = row.cellIterator();
                outerwhile:
                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();
                    XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
                    if (color != null && color.getARGBHex().substring(2).equals(COPY_COLOR)) {
                        Cell templateCell = null;
                        // TODO: ask why some cells don't match
                        try {
                            templateCell = templateSheet.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        } catch (Exception e) {
                            continue outerwhile;
                        }
                        if(templateCell == null) continue;
                        switch (cell.getCellTypeEnum()) {
                            case STRING:
                                templateCell.setCellValue(cell.getStringCellValue());
                                count++;
                                break;
                            case NUMERIC:
                                templateCell.setCellValue(cell.getNumericCellValue());
                                count++;
                                break;
                            case FORMULA:
                                templateCell.setCellFormula(cell.getCellFormula());
                                count++;
                                break;
                            case BLANK:
                            case ERROR:
                                break;
                            default:
                                logger.info("TYPE:" + cell.getCellTypeEnum().name());
                                break;
                        }
                    }
                }
            }
            logger.info("sheet: " + sheet.getSheetName() + " updated cells:" + count);

        }

        // refresh everything
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
        evaluator.evaluateAll();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        templateWb.write(os);
        // templateWb.close();  // Don't do this. You dun wanna save it.
        return os;
    }


    public AccountData readFile(InputStream excelFile) throws IOException {

        AccountData data = new AccountData();
        Workbook workbook = new XSSFWorkbook(excelFile);


        Sheet metadataSheet = workbook.getSheet("Metadata");

        int secIdx = 1;
        for(String sheetName : AccountData.SECTION_LIST) {
            int itemIdx = 1;
            Section section = new Section();
            section.setName(sheetName);
            section.setFontSize((int) metadataSheet.getRow(itemIdx++).getCell(secIdx).getNumericCellValue());
            char ctlChar = metadataSheet.getRow(itemIdx++).getCell(secIdx).getStringCellValue().charAt(0);
            section.setControlColumn(charToIdx(ctlChar));
            char yesNoChar = metadataSheet.getRow(itemIdx++).getCell(secIdx).getStringCellValue().charAt(0);
            section.setYesNoColumn(charToIdx(yesNoChar));

            parseSection(workbook, section);
            data.addSection(section);
            secIdx++;
        }

        Sheet controlSheet = workbook.getSheet("Control");
        // this is D5, put as Row 5 Column D (0 = A1)
        String companyName = controlSheet.getRow(1).getCell(3).getStringCellValue();
        System.out.println(companyName);
        data.setCompanyName(companyName);




        return data;
    }

    private int charToIdx(char ctlChar) {
        return Character.getNumericValue(ctlChar) - Character.getNumericValue('A');
    }

    private void parseSection(Workbook workbook, Section section) {
        Sheet sectionSheet = workbook.getSheet(section.getName());

        boolean isStart = false, isInTable = false;
        Header curHeader = null;
        Table curTable = null;
        StringBuilder startEndBuilder = new StringBuilder("Start line=");

        logger.info("section: " + section.getName() + " Control:" + section.getControlColumn());
        doParse:
        for(int i = 0; ; i++) {
            try {
                Cell cell = sectionSheet.getRow(i).getCell(section.getControlColumn());
                // has control data
                if (cell != null && !StringUtils.isEmpty(cell.getStringCellValue())) {
                    String control = cell.getStringCellValue();
                    switch (control.trim().toLowerCase()) {
                        case Paragraph.HEADING:
                            // case Paragraph.HEADING2:
                            Header h = addRowToSection(section, sectionSheet.getRow(i), new Header());
                            if (curHeader == null) {
                                h.setFirstLine(true);
                            }
                            curHeader = h;
                            break;
                        case Paragraph.START:
                            startEndBuilder.append(i);
                            isStart = true;
                            if (curHeader != null) {
                                curHeader.setLastLine(true);
                            }
                            break;
                        case Paragraph.END:
                            startEndBuilder.append("End line=").append(i);
                            logger.info(startEndBuilder.toString());
                            break doParse;
                        case Paragraph.TABLE_START:
                            isInTable = true;
                            curTable = new Table();
                            section.addSectionElement(curTable);
                            // column widths
                            List<Integer> columnWidths = new ArrayList<>();
                            for (int j = 0; j < section.getControlColumn(); j++) {
                                Cell dataCell = sectionSheet.getRow(i).getCell(j);
                                if (dataCell != null) {
                                    try {
                                        int columnWidth = (int) dataCell.getNumericCellValue();
                                        columnWidths.add(columnWidth);
                                        logger.info("Column width " + section.getName() + " line " + (i + 1) + ", column=" + columnWidth);
                                    } catch (Exception e) {
                                        logger.warn("Unparsable Column Width cell at " + section.getName() + " line " + (i + 1) + ", " + e.toString());
                                    }
                                }
                            }
                            curTable.setColumnWidths(columnWidths);
                            logger.info("Column widths " + section.getName() + " line " + (i + 1) + ", columns=" + columnWidths.size());
                            // row height
                            int rowHeight = (int) sectionSheet.getRow(i).getCell(section.getYesNoColumn()).getNumericCellValue();
                            curTable.setRowHeight(rowHeight);

                            break;
                        case Paragraph.TABLE_END:
                            isInTable = false;
                            break;
                        default:
                            logger.warn("unknown command:" + control.trim());
                            break;
                    }
                }
                // table
                else if (isInTable) {
                    for (int j = 0; j < curTable.getColumnWidths().size(); j++) {
                        Cell dataCell = sectionSheet.getRow(i).getCell(j);
                        if (dataCell != null) {
                            try {
                                switch (dataCell.getCellTypeEnum()) {
                                    case STRING:
                                        curTable.addCell(dataCell.getStringCellValue());
                                        break;
                                    case NUMERIC:
                                        curTable.addCell(""+dataCell.getNumericCellValue());
                                        break;
                                    case FORMULA:
                                        try {
                                            curTable.addCell(dataCell.getStringCellValue());
                                        } catch(Exception e) {
                                            try {
                                                curTable.addCell("" + dataCell.getNumericCellValue());
                                            } catch (Exception e2) {
                                                curTable.addCell("" + dataCell.getCellFormula());
                                            }
                                        }
                                        break;
                                    case BLANK:
                                    case ERROR:
                                    default:
                                        curTable.addCell("");
                                        logger.info("TYPE:" + dataCell.getCellTypeEnum().name());
                                        break;
                                }

                            } catch (Exception e) {
                                logger.warn("Unparsable table content at " + section.getName() + " line " + (sectionSheet.getRow(i).getRowNum() + 1) + ", " + e.toString());
                            }
                        } else {
                            curTable.addCell("");
                        }
                    }
                }
                // rest of contents
                else if (isStart) {
                    Cell yesNoCell = sectionSheet.getRow(i).getCell(section.getYesNoColumn());
                    if (yesNoCell == null) {
                        continue;
                    }
                    String yesNo = Paragraph.NO;
                    try {
                        yesNo = yesNoCell.getStringCellValue();
                    } catch (Exception e) {
                        logger.warn("Unparsable Yes/No cell at " + section.getName() + " line " + (i + 1) + ", " + e.toString());
                    }
                    // ignored row
                    if (yesNo.equalsIgnoreCase(Paragraph.NO))
                        continue;
                    // inside section
                    addRowToSection(section, sectionSheet.getRow(i), new Paragraph());
                }
            } catch (Exception e) {
                logger.error("Error at section " + section.getName() + " line " + (i + 1), e);
                throw e;
            }
        }

    }

    private <T extends Paragraph> T addRowToSection(Section section, Row row, T p) {

        boolean hasContent = false, isBold = false;
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < section.getControlColumn(); i++) {
            Cell dataCell = row.getCell(i);
            if(dataCell != null) {
                try {
                    sb.append(dataCell.getStringCellValue());
                    hasContent = true;
                    XSSFCellStyle style = (XSSFCellStyle) dataCell.getCellStyle();
                    isBold = style.getFont().getBold();
                    p.setBold(isBold);
                } catch (Exception e) {
                    logger.warn("Unparsable content at " + section.getName() + " line " + (row.getRowNum()+1) + ", " + e.toString());
                }
            } else {
                sb.append("\t");   // empty cell
            }
        }
        if(hasContent) {
            p.setText(StringUtils.stripEnd(sb.toString(), null));
        } else {
            p.setText("");
        }
        section.addSectionElement(p);
        return p;
    }


}
