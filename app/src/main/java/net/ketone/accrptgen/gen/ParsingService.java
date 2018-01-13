package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.ss.formula.eval.FunctionEval;
import org.apache.poi.ss.formula.functions.DateDifFunc;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
//                                CellValue cellValue = evaluator.evaluate(cell);
//                                switch (cellValue.getCellTypeEnum()) {
//                                    case BOOLEAN:
//                                        value = "formula: " + cellValue.getBooleanValue();
//                                        break;
//                                    case NUMERIC:
//                                        value = "formula: " + cellValue.getNumberValue();
//                                        break;
//                                    case STRING:
//                                        value = "formula: " + cellValue.getStringValue();
//                                        break;
//                                    case BLANK:
//                                    case ERROR:
//                                        break;
//                                }
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
        FunctionEval.registerFunction("DATEDIF", new DateDifFunc());
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
        Sheet datatypeSheet = workbook.getSheet("Control");

        // this is D5, put as Row 5 Column D (0 = A1)
        String companyName = datatypeSheet.getRow(1).getCell(3).getStringCellValue();
        System.out.println(companyName);
        data.companyName = companyName;
        return data;
    }

}
