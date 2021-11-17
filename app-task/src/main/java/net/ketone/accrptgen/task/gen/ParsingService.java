package net.ketone.accrptgen.task.gen;

import io.vavr.Tuple;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.config.properties.ParseProperties;
import net.ketone.accrptgen.task.gen.generate.BannerService;
import net.ketone.accrptgen.task.util.ExcelTaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

@Slf4j
@Component
public class ParsingService {

    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private BannerService bannerService;

    public XSSFWorkbook postProcess(XSSFWorkbook wb, final ParseProperties properties) {
        FormulaEvaluator inputWbEvaluator = wb.getCreationHelper().createFormulaEvaluator();
        doPostProcess(wb, cell -> stringifyContents(cell, inputWbEvaluator,
                properties.getKeepFormulaColor()));
        doPostProcess(wb, cell -> removeColors(cell));
        return wb;
    }

    private void doPostProcess(XSSFWorkbook wb, Consumer<Cell> f) {
        Iterator<Sheet> i = wb.sheetIterator();
        while(i.hasNext()) {
            Sheet sheet = i.next();
            Iterator<Row> r = sheet.rowIterator();
            while(r.hasNext()) {
                Row row = r.next();
                Iterator<Cell> c = row.cellIterator();
                while(c.hasNext()) {
                    f.accept(c.next());
                }
            }
        }
    }

    private void stringifyContents(Cell cell, FormulaEvaluator inputWbEvaluator,
                                   final String keepFormulaColor) {
        if(Optional.ofNullable(cell.getCellStyle())
                .map(CellStyle::getFillForegroundColorColor)
                .map(XSSFColor::toXSSFColor)
                .map(ExtendedColor::getARGBHex)
                .map(hex -> hex.substring(2))
                .flatMap(color -> Optional.ofNullable(keepFormulaColor)
                        .map(color::equalsIgnoreCase))
                .orElse(Boolean.FALSE)) {
            return;
        }
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


    public XSSFWorkbook deleteSheets(XSSFWorkbook wb, List<String> sheetsToDelete) {
        for(String sheetName : sheetsToDelete) {
            int i = wb.getSheetIndex(sheetName);
            if(i != -1) {   // -1 = not exist
                wb.removeSheetAt(i);
            }
        }
        return wb;
    }

    public XSSFWorkbook cutCells(final XSSFWorkbook wb, Map<String, String> cutCellsMap) {
        wb.sheetIterator().forEachRemaining(sheet -> {
            Optional.ofNullable(cutCellsMap.get(sheet.getSheetName()))
                    .ifPresent(cutCell -> {
                        sheet.rowIterator().forEachRemaining(row -> {
                            CellReference cr = new CellReference(cutCell + row.getRowNum());
                            if(row.getCell(cr.getCol()) != null) {
                                for(int i = cr.getCol(); i <= row.getLastCellNum(); i++) {
                                    final int j = i;
                                    Optional.ofNullable(row.getCell(i))
                                        .ifPresentOrElse(cell -> row.removeCell(cell),
                                                () -> {
                                            log.warn("Empty cell, cannot cut sheet={} cell={}",
                                                    sheet.getSheetName(), new CellReference(row.getRowNum(), j)
                                                            .formatAsString());
                                                });

                                }
                            }
                        });
                    });
        });
        return wb;
    }

    public void insertAuditorBanners(final XSSFWorkbook workbook,
                                      final String auditorName) {
        doPostProcess(workbook, cell -> {
            if("auditorBanner".equals(Try.of(() -> cell.getStringCellValue())
                    .getOrElse(StringUtils.EMPTY)
            )) {
                Optional.ofNullable(bannerService.getImage(auditorName))
                        .ifPresent(is -> ExcelTaskUtils.insertImage(workbook,
                                cell.getSheet().getSheetName(),
                                new org.apache.poi.ss.util.CellReference(cell.getAddress().formatAsString()),
                                Try.of(() -> is.readAllBytes()).get(), Tuple.of(1.001,2.5)));
                cell.setCellValue(StringUtils.EMPTY);
            }
        });
    }
}
