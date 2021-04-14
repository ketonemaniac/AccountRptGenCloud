package net.ketone.accrptgen.service.gen.merge;

import com.google.common.collect.Streams;
import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.exception.GenerationException;
import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.service.gen.FileProcessor;
import net.ketone.accrptgen.service.gen.merge.types.CellTypeProcessor;
import net.ketone.accrptgen.service.store.StorageService;
import net.ketone.accrptgen.util.ExcelUtils;
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

import static net.ketone.accrptgen.util.ExcelUtils.openExcelWorkbook;

@Component
@Slf4j
public class TemplateMergeProcessor {

    private static final List<String> COPY_COLORS = Arrays.asList("4F81BD", "8064A2");

    private static final List<String> preParseSheets =
            Arrays.asList("Control", "Dir info", "Section3", "Section4", "Section6");

    @Autowired
    private SettingsService configurationService;

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

    public byte[] process(byte[] input) throws IOException {
        XSSFWorkbook workbook = openExcelWorkbook(input);
        String templateName = configurationService.getSettings().getProperty(SettingsService.PREPARSE_TEMPLATE_PROP);
        log.info("starting pre-parse to template " + templateName);
        XSSFWorkbook templateWb = Optional.ofNullable(openExcelWorkbook(persistentStorage.loadAsInputStream(
                StorageService.ALLDOCS_PATH + templateName)))
                .orElseThrow(() -> new IOException("Unable to get File " + templateName));
        Map<String, Sheet> templateSheetMap = initSheetMap(templateWb);
        Map<String, Sheet> inputSheetMap = initSheetMap(workbook);
        FormulaEvaluator inputWbEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Flux.fromIterable(inputSheetMap.entrySet())
                .filter(entry -> preParseSheets.contains(entry.getKey()))
                .doOnNext(entry -> log.info("parsing sheet={}", entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(ExcelUtils::cells)
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
        ExcelUtils.evaluateAll(templateWb, new ArrayList<>(templateSheetMap.values()));
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

    private Map<String, Sheet> initSheetMap(Workbook wb) throws IOException {
        return Streams.stream(wb.sheetIterator())
                .collect(Collectors.toMap(Sheet::getSheetName, Function.identity()));
    }

}
