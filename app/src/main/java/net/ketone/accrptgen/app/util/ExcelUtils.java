package net.ketone.accrptgen.app.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.GenerationException;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ExcelUtils {

    public static String extractCompanyName(final XSSFWorkbook workbook, final String sheetName,
                                            final String cellRef) throws IOException {
        CellReference cr = new CellReference(cellRef);
        return Optional.ofNullable(workbook.getSheet(sheetName))
                .map(sheet -> sheet.getRow(cr.getRow()))
                .map(row -> row.getCell(cr.getCol()))
                .map(XSSFCell::getStringCellValue)
                .orElse(StringUtils.EMPTY);
    }

    public static String extractPeriodEnding(final XSSFWorkbook workbook, final String sheetName,
                                             final String cellRef) {
        CellReference cr = new CellReference(cellRef);
        SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyyMM");
        return yyyyMM.format(
                Optional.ofNullable(workbook.getSheet(sheetName))
                .map(sheet -> sheet.getRow(cr.getRow()))
                .map(row -> row.getCell(cr.getCol()))
                .map(XSSFCell::getDateCellValue)
                .orElse(new Date()));
    }
}
