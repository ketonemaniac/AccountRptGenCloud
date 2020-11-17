package net.ketone.accrptgen.service.gen;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.gen.*;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Table;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

// import org.springframework.util.StringUtils;

@Slf4j
@Component
public class ParsingService {

    private static final List<String> COPY_COLORS = Arrays.asList("4F81BD", "8064A2");
    private static final List<String> preParseSheets = Arrays.asList("Control", "Dir info", "Section3", "Section4", "Section6");

    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private CredentialsService credentialsService;


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

    public String extractCompanyName(Workbook workbook) throws IOException {
        Sheet controlSheet = workbook.getSheet("Control");
        // this is D5, put as Row 5 Column D (0 = A1)
        return controlSheet.getRow(1).getCell(3).getStringCellValue();
    }

    public byte[] preParse(Workbook workbook) throws IOException {

        String templateName = credentialsService.getCredentials().getProperty(CredentialsService.PREPARSE_TEMPLATE_PROP);
        log.info("starting pre-parse to template " + templateName);
        InputStream templateStream = persistentStorage.loadAsInputStream(templateName);
        XSSFWorkbook templateWb = new XSSFWorkbook(templateStream);
        templateStream.close();
        if(templateWb == null) {
            throw new IOException("Unable to get File " + templateName);
        }
        Map<String, Sheet> templateSheetMap = initSheetMap(templateWb);

        Map<String, Sheet> inputSheetMap = initSheetMap(workbook);

        FormulaEvaluator inputWbEvaluator = workbook.getCreationHelper().createFormulaEvaluator();

        for(Sheet sheet : inputSheetMap.values()) {
            if(!preParseSheets.contains(sheet.getSheetName())) continue;
            Sheet templateSheet = templateSheetMap.get(sheet.getSheetName());
            if(templateSheet == null) continue;
            log.info("parsing sheet: " + sheet.getSheetName());
            int count = 0;
            for (int r = 0; r < sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Iterator<Cell> cellIter = row.cellIterator();
                outerwhile:
                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();
                    XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
                    if (color != null && COPY_COLORS.contains(color.getARGBHex().substring(2))) {
                        Cell templateCell = null;
                        // TODO: ask why some cells don't match
                        try {
                            templateCell = templateSheet.getRow(cell.getRowIndex()).getCell(cell.getColumnIndex());
                        } catch (Exception e) {
                            continue outerwhile;
                        }
                        if(templateCell == null) continue;
                        switch (cell.getCellTypeEnum()) {
                            // MUST CHANGE THE TYPE FIRST. Otherwise the setCellValue will set the wrong tag.
                            case STRING:
                                templateCell.setCellType(CellType.STRING);
                                templateCell.setCellValue(cell.getStringCellValue());
                                count++;
                                break;
                            case NUMERIC:
                                templateCell.setCellType(CellType.NUMERIC);
                                templateCell.setCellValue(cell.getNumericCellValue());
                                setNumericCellStyle(templateWb, templateCell, cell.getCellStyle());
                                count++;
                                break;
                            case FORMULA:
                                log.info("Formula Sheet=" + sheet.getSheetName() + " Cell=" + cell.getAddress().formatAsString());
                                // templateCell.setCellFormula(cell.getCellFormula());
                                CellValue cellValue = inputWbEvaluator.evaluate(cell);
                                    switch(cellValue.getCellTypeEnum()) {
                                        case NUMERIC:
                                            templateCell.setCellType(CellType.NUMERIC);
                                            templateCell.setCellValue(cellValue.getNumberValue());
                                            setNumericCellStyle(templateWb, templateCell, cell.getCellStyle());
                                            log.info("input cell with formula: " + cell.getCellFormula() + " is now: " + templateCell.getNumericCellValue() + " of type " + cellValue.getCellTypeEnum().name());
                                            templateCell.setCellFormula(null);
                                            break;
                                        default:
                                            try {
                                                // try String for anything else
                                                templateCell.setCellType(CellType.STRING);
                                                templateCell.setCellValue(cellValue.getStringValue());
                                                log.info("input cell with formula: " + cell.getCellFormula() + " is now: " + templateCell.getStringCellValue() + " of type STRING.");
                                                templateCell.setCellFormula(null);
                                            } catch (Exception e2) {
                                                log.warn("cannot evaluate cell with formula: " + cell.getCellFormula() + ". CellType=" + cellValue.getCellTypeEnum().name());
                                                templateCell.setCellValue(cell.getCellFormula());
                                            }
                                            break;
                                    }
                                count++;
                                break;
                            case BOOLEAN:
                                templateCell.setCellType(CellType.BOOLEAN);
                                templateCell.setCellValue(cell.getBooleanCellValue());
                                break;
                            case BLANK:
                            case ERROR:
                                break;
                            default:
                                log.info("TYPE:" + cell.getCellTypeEnum().name());
                                break;
                        }
                    }
                }
            }
            log.info("sheet: " + sheet.getSheetName() + " updated cells:" + count);

        }

        // refresh everything
        log.debug("start refreshing template");
        evaluateAll(templateWb, templateSheetMap);
        log.info("template refreshed. Writing to stream");

        ByteArrayOutputStream os = new ByteArrayOutputStream(1000000);
        log.debug("writing template. os.size()=" + os.size());
        templateWb.write(os);
        log.info("creating byte[] from template. os.size()=" + os.size());
        byte [] result = os.toByteArray();
        log.debug("closing template");
        os.close();
        workbook.close();
        templateWb.close();
        return result;
    }

    /**
     * keep style such as percentages
     * @param srcCellStyle
     * @param templateWb
     * @param templateCell
     */
    private void setNumericCellStyle(XSSFWorkbook templateWb, Cell templateCell, CellStyle srcCellStyle) {
        short df = srcCellStyle.getDataFormat();
        if(df == 170) df = 14;  // I don't want the d/m/yyyy format
        CellStyle tgtCellStyle = templateCell.getCellStyle();
        if(tgtCellStyle == null) {
            tgtCellStyle = templateWb.getStylesSource().createCellStyle();  // must create from styles source
            templateCell.setCellStyle(tgtCellStyle);
        }
        tgtCellStyle.setDataFormat(df);
    }

    /**
     * Same as evaluator.evaluateAll();, but evaluates Cell By Cell making debugging easy.
     * @param templateWb
     * @param templateSheetMap
     */
    private void evaluateAll(XSSFWorkbook templateWb, Map<String, Sheet> templateSheetMap) {
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
//        evaluator.evaluateAll();
        for(Sheet sheet : templateSheetMap.values()) {
            log.info("refreshing sheet: " + sheet.getSheetName());
            int count = 0;
            for (int r = 0; r < sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Iterator<Cell> cellIter = row.cellIterator();
                while (cellIter.hasNext()) {
                    Cell cell = cellIter.next();
                    CellType cellType = null;
                    try {
                        cellType = evaluator.evaluateFormulaCellEnum(cell);
                    } catch(Exception e) {
                        log.warn("cannot evaluate cell " + cell.getAddress().formatAsString() + " with formula: " + cell.getCellFormula() + " cellType=" + cell.getCellTypeEnum().name(), e);
                        throw e;
                    }
                }
            }
        }

    }

    public AccountData readFile(byte[] preParseOutput) throws IOException {

        InputStream excelFile = new ByteArrayInputStream(preParseOutput);
        AccountData data = new AccountData();
        Workbook workbook = new XSSFWorkbook(excelFile);
        data.setCompanyName(extractCompanyName(workbook));


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
        excelFile.close();
        workbook.close();
        return data;
    }

    private int charToIdx(char ctlChar) {
        return Character.getNumericValue(ctlChar) - Character.getNumericValue('A');
    }

    private void parseSection(Workbook workbook, Section section) {
        Sheet sectionSheet = workbook.getSheet(section.getName());

        boolean isStart = false, isInTable = false, isInItem = false;
        Header curHeader = null;
        Table curTable = null;
        StringBuilder startEndBuilder = new StringBuilder("Start line=");

        log.info("section: " + section.getName() + " Control:" + section.getControlColumn());
        doParse:
        for(int i = 0; ; i++) {
            try {
                Cell cell = sectionSheet.getRow(i).getCell(section.getControlColumn());
                // has control data
                if (cell != null && !StringUtils.isEmpty(cell.getStringCellValue())) {
                    String control = cell.getStringCellValue();
                    switch (control.trim().toLowerCase()) {
                        case Paragraph.AUDITOR_HEADING:
                            Header ah = createHeader(section.getName());
                            ah.setAuditorName(sectionSheet.getRow(i).getCell(0).getStringCellValue());
                            section.addSectionElement(ah);
                            break;
                        case Paragraph.AUDITOR_FOOTER:
                            Header af = createHeader(section.getName());
                            af.setAuditorAddress(sectionSheet.getRow(i).getCell(0).getStringCellValue());
                            section.addSectionElement(af);
                            break;
                        case Paragraph.HEADING:
                            // case Paragraph.HEADING2:
                            Header h = addRowToSection(section, sectionSheet.getRow(i), createHeader(section.getName()));
                            if (curHeader == null) {
                                h.setFirstLine(true);
                                if(h.getText().trim().length() > 0) {
                                    h.setLastLine(true);
                                }
                            } else {
                                if(h.getText().trim().length() > 0) {
                                    curHeader.setLastLine(false);
                                    h.setLastLine(true);
                                }
                            }
                            curHeader = h;
                            break;
                        case Paragraph.START:
                            startEndBuilder.append(i);
                            isStart = true;
                            break;
                        case Paragraph.END:
                            startEndBuilder.append("End line=").append(i);
                            log.info(startEndBuilder.toString());
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
                                        log.debug("Column width " + section.getName() + " line " + (i + 1) + ", column=" + columnWidth);
                                    } catch (Exception e) {
                                        log.warn("Unparsable Column Width cell at " + section.getName() + " line " + (i + 1) + ", " + e.toString());
                                    }
                                }
                            }
                            curTable.setColumnWidths(columnWidths);
                            log.info("Column widths " + section.getName() + " line " + (i + 1) + ", columns=" + columnWidths.size());
                            // row height
                            int rowHeight = (int) sectionSheet.getRow(i).getCell(section.getYesNoColumn()).getNumericCellValue();
                            curTable.setRowHeight(rowHeight);

                            break;
                        case Paragraph.TABLE_END:
                            isInTable = false;
                            break;
                        case Paragraph.ITEM:
                            Paragraph p = parseContentRow(sectionSheet, section, i);
                            if(p != null) {
                                p.setItem(true);
                            }
                            isInItem = true;
                            break;
                        default:
                            log.warn("unknown command:" + control.trim());
                            break;
                    }
                }
                // table
                else if (isInTable && isRowIncluded(sectionSheet, section, i)) {
                    for (int j = 0; j < curTable.getColumnWidths().size(); j++) {
                        Cell dataCell = sectionSheet.getRow(i).getCell(j);
                        if (dataCell != null) {
                            Table.Cell parsedCell = null;
                            try {
                                switch (dataCell.getCellTypeEnum()) {
                                    case STRING:
                                        parsedCell = curTable.addCell(dataCell.getStringCellValue());
                                        break;
                                    case NUMERIC:
                                        parsedCell = curTable.addCell(numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle()));
                                        break;
                                    case FORMULA:
                                        try {
                                            parsedCell = curTable.addCell(numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle()));
                                        } catch(Exception e) {
                                            try {
                                                parsedCell = curTable.addCell(dataCell.getStringCellValue());
                                            } catch (Exception e2) {
                                                parsedCell = curTable.addCell(dataCell.getCellFormula());
                                            }
                                        }
                                        break;
                                    case BOOLEAN:
                                        parsedCell = curTable.addCell(Boolean.valueOf(dataCell.getBooleanCellValue()).toString());
                                        break;
                                    case BLANK:
                                        parsedCell = curTable.addCell("");
                                        break;
                                    case ERROR:
                                    default:
                                        parsedCell = curTable.addCell("");
                                        log.warn("TYPE:" + dataCell.getCellTypeEnum().name());
                                        break;
                                }
                                if(dataCell.getCellStyle() == null) continue;
                                XSSFCellStyle style = (XSSFCellStyle) dataCell.getCellStyle();
                                parsedCell.setBold(style.getFont().getBold());
                                parsedCell.setUnderline(style.getFont().getUnderline() == FontUnderline.SINGLE.getByteValue());
                                // underline
                                setCellStyle(dataCell, parsedCell, s -> s.getBorderBottomEnum());
                                // see if the cell below has a top border...
                                setCellStyle(sectionSheet.getRow(i+1).getCell(j), parsedCell, s -> s.getBorderTopEnum());
                                // horizontal alignment
                                switch(style.getAlignmentEnum()) {
                                    case LEFT:
                                        parsedCell.setAlignment(Table.Alignment.LEFT); break;
                                    case CENTER:
                                        parsedCell.setAlignment(Table.Alignment.CENTER); break;
                                    case RIGHT:
                                        parsedCell.setAlignment(Table.Alignment.RIGHT); break;
                                }
                            } catch (Exception e) {
                                log.warn("Unparsable table content at " + section.getName() + " line " + (sectionSheet.getRow(i).getRowNum() + 1) + ", " + e.toString());
                            }
                        } else {
                            Table.Cell blankCell = curTable.addCell("");
                            // see if the cell below has a top border...
                            setCellStyle(sectionSheet.getRow(i+1).getCell(j), blankCell, s -> s.getBorderTopEnum());
                        }
                    }
                }
                // rest of contents
                else if (isStart) {
                     Paragraph p = parseContentRow(sectionSheet, section, i);
                    // indent should automatically add 1 if this is inside numeric lists
                    if(p != null && isInItem) {
                        p.setIndent(p.getIndent()+1);
                    }
                }
            } catch (Exception e) {
                log.error("Error at section " + section.getName() + " line " + (i + 1), e);
                throw e;
            }
        }

    }

    private void setCellStyle(Cell srcCell, Table.Cell parsedCell, Function<XSSFCellStyle, BorderStyle> f) {
        if(srcCell != null && srcCell.getCellStyle() != null) {
            XSSFCellStyle style = (XSSFCellStyle) srcCell.getCellStyle();
            switch(f.apply(style)) {
                case THIN:
                    parsedCell.setBottomBorderStyle(Table.BottomBorderStyle.SINGLE_LINE);
                    break;
                case DOUBLE:
                    parsedCell.setBottomBorderStyle(Table.BottomBorderStyle.DOUBLE_LINE);
                    break;
            }
        }
    }


    private boolean isRowIncluded(Sheet sectionSheet, Section section, int row) {
        Cell yesNoCell = sectionSheet.getRow(row).getCell(section.getYesNoColumn());
        if (yesNoCell == null) {
            return false;
        }
        String yesNo = Paragraph.NO;
        try {
            yesNo = yesNoCell.getStringCellValue();
        } catch (Exception e) {
            log.warn("Unparsable Yes/No cell at " + section.getName() + " line " + (row + 1) + ", " + e.toString());
            return false;
        }
        // ignored row
        if (yesNo.equalsIgnoreCase(Paragraph.NO))
            return false;
        return true;
    }

    private Paragraph parseContentRow(Sheet sectionSheet, Section section, int row) {
        if(isRowIncluded(sectionSheet, section, row)) {
            // inside section
            return addRowToSection(section, sectionSheet.getRow(row), new Paragraph());
        }
        return null;
    }

    private <T extends Paragraph> T addRowToSection(Section section, Row row, T p) {

        boolean hasContent = false, isBold = false;
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < section.getControlColumn(); i++) {
            Cell dataCell = row.getCell(i);
            if(dataCell != null) {
                try {
                    String data = dataCell.getStringCellValue();
                    sb.append(data);
                    // empty cells do not contibute to font
                    if(data.length() > 0) {
                        hasContent = true;
                        XSSFCellStyle style = (XSSFCellStyle) dataCell.getCellStyle();
                        isBold = style.getFont().getBold();
                        p.setBold(isBold);
                    } else if(!hasContent) {
                        // no content, empty cell = indent
                        p.setIndent(p.getIndent()+1);
                    }
                } catch (Exception e) {
                    log.warn("Unparsable content at " + section.getName() + " line " + (row.getRowNum()+1) + ", " + e.toString());
                }
            } else if(!hasContent) {
                // no content, empty cell = indent
                p.setIndent(p.getIndent()+1);
            }
        }
        if(hasContent) {
            p.setText(sb.toString().trim());
        } else {
            p.setText("");
            p.setIndent(0);
        }
        section.addSectionElement(p);
        return p;
    }


    /**
     * Put all predefined header properties here
     * @param section
     * @return
     */
    private Header createHeader(String section) {
        Header header = new Header();
        switch(section) {
            case "Section1":
                header.setHasCompanyName(true);
                header.setUnderline(Header.Underline.NO_UNDERLINE);
                break;
            case "Contents":
                header.setHasCompanyName(true);
                header.setUnderline(Header.Underline.AFTER_LAST);
                break;
            case "Section2":
                header.setHasCompanyName(false);
                header.setUnderline(Header.Underline.AFTER_LAST);
                break;
            case "Section3":
            case "Section4":
            case "Section5":
            case "Section6":
                header.setHasCompanyName(true);
                header.setUnderline(Header.Underline.BEFORE_LAST);
                break;
        }

        return header;
    }

    private String numberFormat(double value, CellStyle cellStyle) {
        if(value == 0) {
            return "-";
        }
        if(cellStyle != null && cellStyle.getDataFormat() == 9) {
            // Percentages
            NumberFormat percentageFormatter = new DecimalFormat("###.##%;(###.##%)");
            return percentageFormatter.format(value);
        }
        NumberFormat myFormatter = new DecimalFormat("###,###,###,###,###;(###,###,###,###,###)");
        String output = myFormatter.format(value);
        return output;
    }


    public Workbook postProcess(XSSFWorkbook wb) {
        FormulaEvaluator inputWbEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        Iterator<Sheet> i = wb.sheetIterator();
        while(i.hasNext()) {
            Sheet sheet = i.next();
            Iterator<Row> r = sheet.rowIterator();
            while(r.hasNext()) {
                Row row = r.next();
                Iterator<Cell> c = row.cellIterator();
                while(c.hasNext()) {
                    Cell cell = c.next();
                    stringifyContents(cell, inputWbEvaluator);
                    removeColors(cell);
                }
            }
        }
        return wb;
    }

    private void stringifyContents(Cell cell, FormulaEvaluator inputWbEvaluator) {
        if(cell.getCellTypeEnum() == CellType.FORMULA) {
            CellValue cellValue = inputWbEvaluator.evaluate(cell);
            cell.setCellType(cellValue.getCellTypeEnum());
            switch(cellValue.getCellTypeEnum()) {
                case NUMERIC:
                    cell.setCellValue(cellValue.getNumberValue());
                    break;
                case BOOLEAN:
                    cell.setCellValue(cellValue.getBooleanValue());
                    break;
                case STRING:
                default:
                    cell.setCellValue(cellValue.getStringValue());
                    break;
            }
        }
    }

    private void removeColors(Cell cell) {
        if(cell.getCellStyle() != null && cell.getCellStyle().getFillPatternEnum() != FillPatternType.NO_FILL) {
            cell.getCellStyle().setFillPattern(FillPatternType.NO_FILL);
            cell.getCellStyle().setFillForegroundColor(IndexedColors.AUTOMATIC.getIndex());
        }
    }


    public Workbook deleteSheets(Workbook wb, List<String> sheetsToDelete) {
        for(String sheetName : sheetsToDelete) {
            int i = wb.getSheetIndex(sheetName);
            if(i != -1) {   // -1 = not exist
                wb.removeSheetAt(i);
            }
        }
        return wb;
    }


}
