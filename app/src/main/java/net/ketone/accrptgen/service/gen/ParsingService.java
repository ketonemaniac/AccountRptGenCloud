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

    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private CredentialsService credentialsService;


    public String extractCompanyName(Workbook workbook) throws IOException {
        Sheet controlSheet = workbook.getSheet("Control");
        // this is D5, put as Row 5 Column D (0 = A1)
        return controlSheet.getRow(1).getCell(3).getStringCellValue();
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
