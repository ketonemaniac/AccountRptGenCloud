package net.ketone.accrptgen.task.excelextract;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.task.config.properties.ExcelExtractProperties;
import net.ketone.accrptgen.task.gen.ParsingService;
import net.ketone.accrptgen.task.gen.merge.TemplateMergeProcessor;
import net.ketone.accrptgen.task.util.ExcelUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ExcelExtractTask {

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


    public void doExcelExtract(final AccountJob job) {
        String inputFileName = job.getFilename();
        log.info("Opening file: " + inputFileName);
        try {
            String fileExtension = inputFileName.substring(inputFileName.lastIndexOf("."));
            byte[] workbookArr = tempStorage.load(inputFileName);
            byte[] preParseOutput = templateMergeProcessor.process(workbookArr, properties.getMerge());

            Workbook stringifiedWorkbook = parsingService.postProcess(
                    ExcelUtils.openExcelWorkbook(preParseOutput));
            Map<String, String> cutColumnsMap = extractCutColumns(stringifiedWorkbook);

            Workbook finalOutput = parsingService.deleteSheets(
                    stringifiedWorkbook, Arrays.asList("metadata", "Control"));
            Workbook finalFinal = parsingService.cutCells(finalOutput, cutColumnsMap);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            finalFinal.write(os);

            Attachment template = new Attachment(job.getCompany() +
                    job.getGenerationTime().toString() +
                    fileExtension, os.toByteArray());
            List<Attachment> attachments = Arrays.asList(template);
            emailService.sendEmail(job, attachments);

            // no need to use the template anymore, delete it.
            tempStorage.delete(inputFileName);

            job.setStatus(Constants.Status.EMAIL_SENT.name());
            statisticsService.updateTask(job);
            log.info("Operation complete");

        } catch (Exception e) {
            log.error("DIE", e);
        }
    }

    private Map<String, String> extractCutColumns(final Workbook workbook) {
        Map<String, String> cutRowsMap = new HashMap<>();
        Sheet metadataSheet = workbook.getSheet("metadata");
        Row cutRow = metadataSheet.getRow(4);
        metadataSheet.getRow(3).cellIterator().forEachRemaining(
                cell -> {
                    log.info("col={}, value={}", cell.getAddress().getColumn(),
                            cell.getStringCellValue());
                    log.info("cutRow={}", cutRow.getCell(cell.getAddress().getColumn()));
                    cutRowsMap.put(cell.getStringCellValue(),
                            cutRow.getCell(cell.getAddress().getColumn()).getStringCellValue());
                }
        );
        return cutRowsMap;
    }
}
