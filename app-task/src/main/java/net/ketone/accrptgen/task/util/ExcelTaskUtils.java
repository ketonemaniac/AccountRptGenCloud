package net.ketone.accrptgen.task.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.GenerationException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ExcelTaskUtils {

    public static XSSFWorkbook openExcelWorkbook(final byte[] workbookArr) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(workbookArr));
    }

    public static XSSFWorkbook openExcelWorkbook(final InputStream workbookStream) throws IOException {
        try(workbookStream) {
            return new XSSFWorkbook(workbookStream);
        } finally {
            workbookStream.close();
        }
    }

    /**
     * Same as evaluator.evaluateAll();, but evaluates Cell By Cell making debugging easy.
     * @param templateWb
     * @param sheets
     */
    public static void evaluateAll(XSSFWorkbook templateWb, List<Sheet> sheets) {
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
//        evaluator.evaluateAll();
        Flux.fromIterable(sheets)
                .doOnNext(sheet -> log.info("refreshing sheet={}", sheet.getSheetName()))
                .flatMap(ExcelTaskUtils::cells)
                .map(Tuple2::_2)
                .flatMap(cell -> Mono.just(cell)
                        .map(evaluator::evaluateFormulaCellEnum)
                        .onErrorMap(err -> new GenerationException(cell.getSheet().getSheetName(),
                                cell.getAddress().formatAsString(),
                                "TemplateMerge",
                                String.format("Cannot evaluate Sheet: %s Cell: %s with formula: %s cellType: %s",
                                        cell.getSheet().getSheetName(),
                                        cell.getAddress().formatAsString(),
                                         cell.getCellFormula(),
                                        cell.getCellTypeEnum().name()),
                                err.getMessage(),
                                err))
                )
                .blockLast();
    }

    /**
     * Gets all cells associated to a sheet
     * @param s
     * @return
     */
    public static Flux<Tuple2<Sheet, Cell>> cells(final Sheet s) {
        return Mono.just(s)
                .flatMapMany(sheet -> Flux.range(0, sheet.getLastRowNum())
                        .filter(r -> Optional.ofNullable(sheet.getRow(r)).isPresent())
                        .map(sheet::getRow)
                )
                .flatMap(row -> Flux.fromIterable(row::cellIterator))
                .map(cell -> Tuple.of(s, (Cell) cell));
    }


    /**
     *
     * @param workbook
     * @param position
     * @param rawPng
     * @param sizeCellSpan cell span, somehow 1.0 doesn't work
     */
    public static void insertImage(final Workbook workbook, final String sheetName,
                                   final CellReference position,
                            final byte[] rawPng, final Tuple2<Double, Double> sizeCellSpan) {
        int pictureIdx = workbook.addPicture(rawPng, Workbook.PICTURE_TYPE_PNG);
        Sheet sheet = workbook.getSheet(sheetName);
        CreationHelper helper = workbook.getCreationHelper();
        Drawing drawing = sheet.createDrawingPatriarch();
        ClientAnchor anchor = helper.createClientAnchor();
        anchor.setCol1(position.getCol());
        anchor.setRow1(position.getRow());
        Picture picture = drawing.createPicture(anchor, pictureIdx);
        picture.resize(sizeCellSpan._1,sizeCellSpan._2);
    }
}
