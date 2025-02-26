package net.ketone.accrptgen.common.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.EvaluationException;
import org.apache.commons.collections4.IteratorUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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


    public static void evaluateAll(final String stage, XSSFWorkbook templateWb, final boolean isDefaultKeepFormula) {
        evaluateAll(stage, templateWb, null, isDefaultKeepFormula);
    }

    public static Flux<Cell> loopingEveryCell(final String location, XSSFWorkbook templateWb, Consumer<Cell> cellAction) {
        return loopingCells(location, templateWb, cellAction, (ignore) -> true);
    }

    public static Flux<Cell> loopingCells(final String location, XSSFWorkbook templateWb, Consumer<Cell> cellAction,
                                          final Function<String, Boolean> sheetFilter) {
        return Flux.fromIterable(IteratorUtils.toList(templateWb.sheetIterator()))
                .filter(sheet -> sheetFilter.apply(sheet.getSheetName()))
                .doOnNext(sheet -> log.info("{} in sheet={}", location, sheet.getSheetName()))
                .concatMap(sheet -> Flux.fromIterable(IteratorUtils.toList(sheet.rowIterator())))
                .concatMap(row -> Flux.fromIterable(IteratorUtils.toList(row.cellIterator())))
                .doOnNext(cellAction);
    }

    /**
     * Loops and calculates/replaces formula contents with evaluated value, depending on whether the keepFormulaColor is matched
     * @see https://poi.apache.org/apidocs/3.17/org/apache/poi/ss/usermodel/FormulaEvaluator.html#evaluateInCell(org.apache.poi.ss.usermodel.Cell)
     * If you want the cell replaced with the result of the formula, use evaluateInCell(Cell)
     * @param templateWb
     */
    public static void evaluateAll(final String stage, XSSFWorkbook templateWb, final String keepFormulaColor,
                                   final boolean isDefaultKeepFormula) {
        evaluateSheets(stage, templateWb, keepFormulaColor, (ignore) -> true, isDefaultKeepFormula);
    }

    public static void evaluateSheets(final String stage, XSSFWorkbook templateWb, final String keepFormulaColor,
                                      final Function<String, Boolean> sheetFilter, final boolean isDefaultKeepFormula) {
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
        List<EvaluationException> exceptions = new ArrayList<>();
        loopingCells(stage, templateWb, cell -> {
            if(cell.getCellType() != CellType.FORMULA) return;      // no need to evaluate non-formula cells
            CellType evaluationResult;
            if (Optional.ofNullable(cell.getCellStyle())
                    .map(CellStyle::getFillForegroundColorColor)
                    .map(XSSFColor::toXSSFColor)
                    .map(ExtendedColor::getARGBHex)
                    .map(hex -> hex.substring(2))
                    .flatMap(color -> Optional.ofNullable(keepFormulaColor)
                            .map(color::equalsIgnoreCase))
                    .orElse(isDefaultKeepFormula)) {
                // The type of the formula result, i.e. -1 if the cell is not a formula, or one of CellType.NUMERIC, CellType.STRING, CellType.BOOLEAN, CellType.ERROR
                // Note: the cell's type remains as CellType.FORMULA however
                evaluationResult = evaluator.evaluateFormulaCell(cell);
            } else {
                // If cell contains formula, it evaluates the formula, and puts the formula result back into the cell, in place of the old formula.
                // Be aware that your cell value will be changed to hold the result of the formula.
                evaluationResult = Try.ofSupplier(() -> evaluator.evaluateInCell(cell).getCellType())
                        .onFailure(err -> log.error(err.toString()))
                        .getOrElse(CellType.ERROR);

            };
            if (evaluationResult.equals(CellType.ERROR)) {
                exceptions.add(new EvaluationException(stage, cell));
            }
        }, sheetFilter)
                .blockLast();
        if(!exceptions.isEmpty()) {
            throw new RuntimeException(String.format("%s %s",
                    "Cannot evaluate cells: ",
                    exceptions.stream().map(EvaluationException::getLocation).collect(Collectors.joining(", "))));
        }
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

    public static byte[] saveExcelToBytes(XSSFWorkbook wbToSave) throws IOException {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream(1000000);
            log.debug("writing template. os.size()=" + os.size());
            wbToSave.write(os);
            log.info("creating byte[] from template. os.size()=" + os.size());
            byte[] result = os.toByteArray();
            log.debug("closing template");
            return result;
        } finally {
            wbToSave.close();
        }
    }

    public static List<String> matchSheetsWithRegex(XSSFWorkbook workbook, List<String> regexs) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(workbook.sheetIterator(), Spliterator.ORDERED),
                false)
                .filter(sheet -> {
                    for(String auditSheetName : regexs) {
                        var a = Pattern.compile(auditSheetName).matcher(sheet.getSheetName());
                        if(a.find()) return true;
                    }
                    return false;
                })
                .map(Sheet::getSheetName)
                .toList();
    }

    public static XSSFWorkbook deleteSheets(XSSFWorkbook wb, List<String> sheetsToDelete) {
        for(String sheetName : sheetsToDelete) {
            int i = wb.getSheetIndex(sheetName);
            if(i != -1) {   // -1 = not exist
                wb.removeSheetAt(i);
            }
        }
        return wb;
    }

    public static XSSFWorkbook retainSheets(XSSFWorkbook wb, List<String> sheetsToRetain) {
        List<String> sheetsToDelete = new ArrayList<>();
        wb.sheetIterator().forEachRemaining(sheet -> {
            if(!sheetsToRetain.contains(sheet.getSheetName())) {
                sheetsToDelete.add(sheet.getSheetName());
            }
        });
        return deleteSheets(wb, sheetsToDelete);
    }

}
