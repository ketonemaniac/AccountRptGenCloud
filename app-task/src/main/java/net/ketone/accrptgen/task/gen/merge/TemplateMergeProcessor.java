package net.ketone.accrptgen.task.gen.merge;

import com.google.common.collect.Streams;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.config.properties.MergeProperties;
import net.ketone.accrptgen.task.gen.merge.types.CellTypeProcessor;
import net.ketone.accrptgen.task.util.ExcelTaskUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TemplateMergeProcessor {

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

    public byte[] process(byte[] input, final MergeProperties properties) throws IOException {
        XSSFWorkbook workbook = ExcelTaskUtils.openExcelWorkbook(input);
        XSSFWorkbook templateWb = Optional.ofNullable(configurationService.getSettings())
                .flatMap(settings -> Optional.ofNullable(properties.getTemplateFileProperty())
                        .map(templateFile -> settings.getProperty(templateFile)))
                .flatMap(templateName ->
                        Optional.ofNullable(properties.getTemplatePath())
                        .map(path -> Try.of(() -> ExcelTaskUtils.openExcelWorkbook(
                                persistentStorage.loadAsInputStream(
                                        path + File.separator + templateName)))
                        ))
                .orElseThrow(() -> new IOException("Unable to get File from properties " + properties))
                .getOrElseThrow((err) -> new RuntimeException("Fail to open file: " + err));
        Map<String, Sheet> templateSheetMap = initSheetMap(templateWb);
        Map<String, Sheet> inputSheetMap = initSheetMap(workbook);
        FormulaEvaluator inputWbEvaluator = workbook.getCreationHelper().createFormulaEvaluator();
        Flux.fromIterable(inputSheetMap.entrySet())
                .filter(entry -> properties.getPreParseSheets().contains(entry.getKey()))
                .doOnNext(entry -> log.info("parsing sheet={}", entry.getKey()))
                .map(Map.Entry::getValue)
                .flatMap(ExcelTaskUtils::cells)
                .filter(tuple2 -> {
                    XSSFColor color = XSSFColor.toXSSFColor(tuple2._2.getCellStyle().getFillForegroundColorColor());
                    return color != null && properties.getMergeCellColors().contains(color.getARGBHex().substring(2));
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
        ExcelTaskUtils.evaluateAll(templateWb, new ArrayList<>(templateSheetMap.values()));
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
