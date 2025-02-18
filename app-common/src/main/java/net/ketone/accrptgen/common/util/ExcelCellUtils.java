package net.ketone.accrptgen.common.util;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.CellValueHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.StreamSupport;

@Slf4j
public class ExcelCellUtils {

    private static final String MISSING_INFO = "Please provide data(Col %s) with title(Col A) '%s' of sheet '%s'";

    public static final String colStrMap = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static String extractByTitleCellName(final XSSFWorkbook workbook, final String sheetName,
                                                    final String titleCellName, final int dataColumn) {
       return Optional.ofNullable(workbook.getSheet(sheetName))
                .map(sheet -> sheet.rowIterator())
                .map(iterator -> StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(
                                iterator, Spliterator.ORDERED), false)
                        .filter(row -> row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                            .getStringCellValue().trim().startsWith(titleCellName))
                        .findFirst())
                .flatMap(Function.identity())
                .map(row -> row.getCell(dataColumn))
                .map(cell -> cell.getAddress().formatAsString())
                .flatMap(cellRef -> extract(workbook, sheetName, cellRef))
                .orElseThrow(() -> new RuntimeException(String.format(MISSING_INFO, colStrMap.charAt(dataColumn),
                        titleCellName, sheetName)));
    }

    public static Optional<String> extract(final XSSFWorkbook workbook, final String sheetName,
                                             final String cellRef) {
        CellReference cr = new CellReference(cellRef);
        return Optional.ofNullable(workbook.getSheet(sheetName))
                .map(sheet -> sheet.getRow(cr.getRow()))
                .map(row -> row.getCell(cr.getCol()))
                .map(ExcelCellUtils::getCellValue)
                .map(cell -> {
                    switch (cell.getCellType()) {
                        case DATE:
                            SimpleDateFormat yyyyMM = new SimpleDateFormat("yyyyMMdd");
                            return yyyyMM.format(cell.getDateVal());
                        case NUMERIC:
                            return String.valueOf(cell.getNumVal());
                        case STRING:
                            return cell.getStrVal();
                        default:
                            return StringUtils.EMPTY;
                    }
                });
    }

    public static CellValueHolder getCellValue(Cell c) {
        String dataFormatStr = Optional.ofNullable(c.getCellStyle()).map(CellStyle::getDataFormatString)
                .orElse(StringUtils.EMPTY);
        if(CellType.NUMERIC.equals(c.getCellType()) &&
                dataFormatStr.contains("y") && dataFormatStr.contains("m") && dataFormatStr.contains("d")) {
            // this should be a date type
            return CellValueHolder.builder()
                    .cellType(CellValueHolder.CellType.DATE)
                    .dateVal(c.getDateCellValue())
                    .build();
        }
        // numeric or String
        return Try.of(() -> c.getNumericCellValue()).map(val ->
                CellValueHolder.builder()
                        .cellType(CellValueHolder.CellType.NUMERIC)
                        .numVal(val)
                        .build())
                .getOrElse(() ->
                        CellValueHolder.builder()
                                .cellType(CellValueHolder.CellType.STRING)
                                .strVal(c.getStringCellValue())
                                .build()
                );
    }
}
