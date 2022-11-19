package net.ketone.accrptgen.task.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.GenerationException;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.function.Consumer;

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


    public static void evaluateAll(final String stage, XSSFWorkbook templateWb) {
        evaluateAll(stage, templateWb, null);
    }

    public static Flux<Cell> loopingEveryCell(final String location, XSSFWorkbook templateWb, Consumer<Cell> cellAction) {
        return Flux.fromIterable(IteratorUtils.toList(templateWb.sheetIterator()))
                .doOnNext(sheet -> log.info("{} in sheet={}", location, sheet.getSheetName()))
                .concatMap(sheet -> Flux.fromIterable(IteratorUtils.toList(sheet.rowIterator())))
                .concatMap(row -> Flux.fromIterable(IteratorUtils.toList(row.cellIterator())))
                .doOnNext(cellAction);
    }


    /**
     * Loops and calculates/replaces formula contents with evaluated value, depending on whether the keepFormulaColor is matched
     * @see https://poi.apache.org/apidocs/3.17/org/apache/poi/ss/usermodel/FormulaEvaluator.html#evaluateInCell(org.apache.poi.ss.usermodel.Cell)
     * @param templateWb
     */
    public static void evaluateAll(final String stage, XSSFWorkbook templateWb, final String keepFormulaColor) {
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
        loopingEveryCell(stage, templateWb, cell ->
                Try.ofCallable(() -> {
                                if(Optional.ofNullable(cell.getCellStyle())
                                        .map(CellStyle::getFillForegroundColorColor)
                                        .map(XSSFColor::toXSSFColor)
                                        .map(ExtendedColor::getARGBHex)
                                        .map(hex -> hex.substring(2))
                                        .flatMap(color -> Optional.ofNullable(keepFormulaColor)
                                                .map(color::equalsIgnoreCase))
                                        .orElse(Boolean.FALSE)) {
                                    return evaluator.evaluateFormulaCellEnum(cell);
                                } else {
                                    return evaluator.evaluateInCell(cell).getCellTypeEnum();
                                }
                        })
                        .andThen(cellTypeEnum -> {
                            if (cellTypeEnum.equals(CellType.ERROR)) {
                                throw new RuntimeException("Evaluation ended in ERROR");
                            }
                        })
                        .getOrElseThrow(err -> new GenerationException(cell.getSheet().getSheetName(),
                                cell.getAddress().formatAsString(),
                                stage,
                                String.format("%s: Cannot evaluate Sheet: %s Cell: %s ",
                                        stage,
                                        cell.getSheet().getSheetName(),
                                        cell.getAddress().formatAsString(),
                                        cell.getCellTypeEnum().name())))
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
                .flatMapMany(sheet -> Flux.range(0, sheet.getLastRowNum()+1)
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
