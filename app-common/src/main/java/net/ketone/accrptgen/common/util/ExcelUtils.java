package net.ketone.accrptgen.common.util;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.CellValueHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.text.SimpleDateFormat;
import java.util.Optional;

@Slf4j
public class ExcelUtils {

    public static String extract(final XSSFWorkbook workbook, final String sheetName,
                                             final String cellRef) {
        CellReference cr = new CellReference(cellRef);
        return Optional.ofNullable(workbook.getSheet(sheetName))
                .map(sheet -> sheet.getRow(cr.getRow()))
                .map(row -> row.getCell(cr.getCol()))
                .map(ExcelUtils::getCellValue)
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
                })
                .orElse(StringUtils.EMPTY);
    }

    public static CellValueHolder getCellValue(Cell c) {
        String dataFormatStr = Optional.ofNullable(c.getCellStyle()).map(CellStyle::getDataFormatString)
                .orElse(StringUtils.EMPTY);
        if(CellType.NUMERIC.equals(c.getCellTypeEnum()) &&
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
