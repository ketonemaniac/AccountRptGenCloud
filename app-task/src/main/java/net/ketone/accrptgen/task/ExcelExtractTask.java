package net.ketone.accrptgen.task;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.FileUtils;
import net.ketone.accrptgen.task.config.properties.ExcelExtractProperties;
import net.ketone.accrptgen.task.gen.ParsingService;
import net.ketone.accrptgen.task.gen.merge.TemplateMergeProcessor;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static net.ketone.accrptgen.common.util.SSEUtils.toSSE;

@Slf4j
@Component
@Scope("prototype")
public class ExcelExtractTask implements Runnable {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TemplateMergeProcessor templateMergeProcessor;
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private ExcelExtractProperties properties;

    private static List<String> CONTROL_SHEETS = Arrays.asList("metadata", "Control");

    private AccountJob job;

    public ExcelExtractTask(final AccountJob job) {
        this.job = job;
    }

    @Override
    public void run() {
         String inputFileName = job.getClientRandInt() + job.getFilename().substring(job.getFilename().lastIndexOf("."));
        log.info("Opening file: " + inputFileName);
        String outputFilename = FileUtils.uniqueFilename(job.getCompany(), job.getGenerationTime());
        String fileExtension = inputFileName.substring(inputFileName.lastIndexOf("."));
        try {
            byte[] workbookArr = tempStorage.load(inputFileName);
            properties.getMerge().setPreParseSheets(sheets(workbookArr));
            XSSFWorkbook preParseOutput = templateMergeProcessor.process(workbookArr, properties.getMerge());
            log.debug("start refreshing preParseOutput");
            ExcelTaskUtils.evaluateAll("TemplateMergeProcessor", preParseOutput, properties.getMerge().getKeepFormulaColor(), false);

            // parse and stringify contents
            XSSFWorkbook stringifiedWorkbook = parsingService.postProcess(preParseOutput, properties.getParse());

            Map<String, String> cutColumnsMap = extractCutColumns(stringifiedWorkbook);

            // remove sheets according to cutColumnsMap
            XSSFWorkbook finalOutput = ExcelTaskUtils.retainSheets(stringifiedWorkbook,
                                                    new ArrayList<>(cutColumnsMap.keySet()));

            // cut cells according to cutColumnsMap
            XSSFWorkbook finalFinal = parsingService.cutCells(finalOutput, cutColumnsMap);
            parsingService.insertAuditorBanners(finalFinal, job.getAuditorName());

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            finalFinal.write(os);

            Attachment template = new Attachment( outputFilename +
                    fileExtension, os.toByteArray());
            List<Attachment> attachments = Arrays.asList(template);
            emailService.sendEmail(job, attachments, properties.getMail());

            // no need to use the template anymore, delete it.
            tempStorage.delete(inputFileName);

            // store them in temp storage
            tempStorage.store(os.toByteArray(), outputFilename + fileExtension);

            job.setStatus(Constants.Status.GENERATED.name());
            job.setFilename(outputFilename + fileExtension);
           statisticsService.updateTask(job);
            log.info("Operation complete");

        } catch (Exception e) {
            log.warn("Generation failed", e);
            job.setFilename(outputFilename + fileExtension);
            job.setStatus(Constants.Status.FAILED.name());
            try {
                job.setErrorMsg(e.getMessage());
                statisticsService.updateTask(job);
            } catch (Throwable e1) {
                log.warn("History file write failed", e1);
            }
        }
    }

    private Map<String, String> extractCutColumns(final Workbook workbook) {
        Map<String, String> cutRowsMap = new HashMap<>();
        Sheet metadataSheet = workbook.getSheet("metadata");
        Row cutRow = metadataSheet.getRow(4);
        metadataSheet.getRow(3).cellIterator().forEachRemaining(
                cell -> {
                    Optional.ofNullable(cell)
                            .map(Cell::getStringCellValue)
                            .map(String::trim)
                            .filter(StringUtils::isNotEmpty)
                            .ifPresentOrElse(
                                    sheetName -> {
                                        String cutCell = Optional.ofNullable(cell.getAddress().getColumn())
                                                .map(cutRow::getCell)
                                                .map(Cell::getStringCellValue)
                                                .orElse(StringUtils.EMPTY);
                                        log.info("metadata cell={}, sheet={} cut={}", cell.getAddress(), sheetName,
                                                cutCell);
                                        cutRowsMap.put(sheetName, cutCell);
                                    },
                                    () -> {
                                        log.info("metadata cell without value: col={}", cell.getAddress());
                                    }
                            );

                }
        );
        return cutRowsMap;
    }

    private static List<String> sheets(final byte[] workbookArr) {
        return Try.of(() ->
                StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                        ExcelTaskUtils.openExcelWorkbook(workbookArr).sheetIterator(), Spliterator.ORDERED), false)
                .map(Sheet::getSheetName)
                .collect(Collectors.toList()))
        .getOrElse(List.of());
    }

}
