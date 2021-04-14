package net.ketone.accrptgen.util;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.exception.GenerationException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ExcelUtils {

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
                .flatMap(ExcelUtils::cells)
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
                .map(cell -> Tuple.of(s, cell));
    }
}
