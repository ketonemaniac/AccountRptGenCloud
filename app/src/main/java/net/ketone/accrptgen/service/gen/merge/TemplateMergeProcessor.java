package net.ketone.accrptgen.service.gen.merge;

import com.google.common.collect.Streams;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.exception.GenerationException;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.service.gen.FileProcessor;
import net.ketone.accrptgen.service.gen.merge.types.CellTypeProcessor;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import io.vavr.Tuple2;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TemplateMergeProcessor implements FileProcessor<byte[]> {

    private static final List<String> COPY_COLORS = Arrays.asList("4F81BD", "8064A2");

    private static final List<String> preParseSheets =
            Arrays.asList("Control", "Dir info", "Section3", "Section4", "Section6");

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private StorageService persistentStorage;

    @Autowired
    private List<CellTypeProcessor> cellTypeProcessors;

    private Map<CellType, CellTypeProcessor> cellTypeProcessorMap;

    @PostConstruct
    public void init() {
        cellTypeProcessorMap = cellTypeProcessors.stream().collect(Collectors.toMap(
                CellTypeProcessor::getCellType, Function.identity()));
    }

    @Override
    public byte[] process(byte[] input) throws IOException {
        XSSFWorkbook workbook = openExcelWorkbook(input);
        String templateName = credentialsService.getCredentials().getProperty(CredentialsService.PREPARSE_TEMPLATE_PROP);
        log.info("starting pre-parse to template " + templateName);
        XSSFWorkbook templateWb = Optional.ofNullable(openExcelWorkbook(persistentStorage.loadAsInputStream(templateName)))
                .orElseThrow(() -> new IOException("Unable to get File " + templateName));
        Map<String, Sheet> templateSheetMap = initSheetMap(templateWb);
        Map<String, Sheet> inputSheetMap = initSheetMap(workbook);
        FormulaEvaluator inputWbEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Flux.fromIterable(inputSheetMap.entrySet())
                .filter(entry -> preParseSheets.contains(entry.getKey()))
                .doOnNext(entry -> log.info("parsing sheet={}", entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(this::cells)
                .filter(tuple2 -> {
                    XSSFColor color = XSSFColor.toXSSFColor(tuple2._2.getCellStyle().getFillForegroundColorColor());
                    return color != null && COPY_COLORS.contains(color.getARGBHex().substring(2));
                })
                .map(tuple2 -> tuple2.append(Optional.ofNullable(templateSheetMap.get(tuple2._1.getSheetName()))
                                .map(sheet -> sheet.getRow(tuple2._2.getRowIndex()))
                                .map(row -> row.getCell(tuple2._2.getColumnIndex()))
                                .orElse(null)))
                .filter(tuple3 -> Optional.ofNullable(tuple3._3).isPresent())
                .map(tuple3 -> tuple3.append(Optional.ofNullable(cellTypeProcessorMap.get(tuple3._2.getCellTypeEnum()))
                        .orElse(cellTypeProcessorMap.get(CellType._NONE)))
                )
                .doOnNext(tuple4 -> tuple4._4.visit(CellInfo.builder()
                                .workbook(workbook)
                                .sheet(tuple4._1)
                                .cell(tuple4._2)
                                .evaluator(inputWbEvaluator)
                                .build()
                        , CellInfo.builder()
                                .workbook(templateWb)
                                .sheet(tuple4._1)
                                .cell(tuple4._3)
                                .build()))
                .blockLast();

        // refresh everything
        log.debug("start refreshing template");
        evaluateAll(templateWb, templateSheetMap);
        log.info("template refreshed. Writing to stream");
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000000);
        log.debug("writing template. os.size()=" + os.size());
        templateWb.write(os);
        log.info("creating byte[] from template. os.size()=" + os.size());
        byte [] result = os.toByteArray();
        log.debug("closing template");
        workbook.close();
        templateWb.close();
        return result;
    }

    /**
     * Gets all cells associated to a sheet
     * @param s
     * @return
     */
    private Flux<Tuple2<Sheet, Cell>> cells(final Sheet s) {
        return Mono.just(s)
                .flatMapMany(sheet -> Flux.range(0, sheet.getLastRowNum())
                        .filter(r -> Optional.ofNullable(sheet.getRow(r)).isPresent())
                        .map(sheet::getRow)
                )
                .flatMap(row -> Flux.fromIterable(row::cellIterator))
                .map(cell -> Tuple.of(s, cell));
    }

    /**
     * Same as evaluator.evaluateAll();, but evaluates Cell By Cell making debugging easy.
     * @param templateWb
     * @param templateSheetMap
     */
    private void evaluateAll(XSSFWorkbook templateWb, Map<String, Sheet> templateSheetMap) {
        FormulaEvaluator evaluator = templateWb.getCreationHelper().createFormulaEvaluator();
        evaluator.clearAllCachedResultValues();
//        evaluator.evaluateAll();
        Flux.fromIterable(templateSheetMap.values())
                .doOnNext(sheet -> log.info("refreshing sheet={}", sheet.getSheetName()))
                .flatMap(this::cells)
                .map(Tuple2::_2)
                .flatMap(cell -> Mono.just(cell)
                        .map(evaluator::evaluateFormulaCellEnum)
                        .doOnError(err -> {
                            log.error("cannot evaluate cell " + cell.getAddress().formatAsString() +
                                    " with formula: " + cell.getCellFormula() +
                                    " cellType=" + cell.getCellTypeEnum().name(), err);
                        })
                        .onErrorMap(err -> new GenerationException(cell.getSheet().getSheetName(),
                                cell.getAddress().formatAsString(),
                                "TemplateMerge",
                                String.format("Cannot evaluate formula: %s", cell.getCellFormula()),
                                err.getMessage(),
                                err))
                )
                .blockLast();
    }


    private Map<String, Sheet> initSheetMap(Workbook wb) throws IOException {
        return Streams.stream(wb.sheetIterator())
                .collect(Collectors.toMap(Sheet::getSheetName, Function.identity()));
    }

}
