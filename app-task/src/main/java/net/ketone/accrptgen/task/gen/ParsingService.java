package net.ketone.accrptgen.task.gen;

import io.vavr.Tuple;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.config.properties.ParseProperties;
import net.ketone.accrptgen.task.gen.generate.BannerService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class ParsingService {

    @Autowired
    private StorageService persistentStorage;
    @Autowired
    private BannerService bannerService;

    public XSSFWorkbook postProcess(XSSFWorkbook wb, final ParseProperties properties) {
        ExcelTaskUtils.loopingEveryCell("postProcess.removeColors", wb, this::removeColors).blockLast();
        return wb;
    }

    private void removeColors(Cell cell) {
        if(cell.getCellStyle() != null && cell.getCellStyle().getFillPattern() != FillPatternType.NO_FILL) {
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

    public XSSFWorkbook retainSheets(XSSFWorkbook wb, List<String> sheetsToRetain) {
        List<String> sheetsToDelete = new ArrayList<>();
        wb.sheetIterator().forEachRemaining(sheet -> {
            if(!sheetsToRetain.contains(sheet.getSheetName())) {
                sheetsToDelete.add(sheet.getSheetName());
            }
        });
        return deleteSheets(wb, sheetsToDelete);
    }

    public XSSFWorkbook cutCells(final XSSFWorkbook wb, Map<String, String> cutCellsMap) {
        wb.sheetIterator().forEachRemaining(sheet -> {
            Optional.ofNullable(cutCellsMap.get(sheet.getSheetName()))
                    .filter(StringUtils::isNotEmpty)
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
        ExcelTaskUtils.loopingEveryCell("ParsingService.insertAuditorBanners", workbook, cell -> {
            if("auditorBanner".equals(Try.of(() -> cell.getStringCellValue())
                    .getOrElse(StringUtils.EMPTY)
            )) {
                bannerService.getImage(auditorName)
                        .ifPresent(is -> ExcelTaskUtils.insertImage(workbook,
                                cell.getSheet().getSheetName(),
                                new org.apache.poi.ss.util.CellReference(cell.getAddress().formatAsString()),
                                Try.of(() -> is.readAllBytes()).get(), Tuple.of(1.001,2.5)));
                cell.setCellValue(StringUtils.EMPTY);
            }
        }).blockLast();
    }
}
