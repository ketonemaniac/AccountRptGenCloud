package net.ketone.accrptgen.app.service.tasks;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.util.ExcelUtils;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.app.service.gen.ParsingService;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.app.util.UserUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class TaskSubmissionService {

    @Autowired
    private ParsingService parsingService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private StorageService tempStorage;
    @Autowired
    private TasksService tasksService;

    public AccountJob triage(final String fileExtension, final byte[] fileBytes) throws IOException {
        long curTimeMs = System.currentTimeMillis();
        tempStorage.store(fileBytes, curTimeMs + fileExtension);
        AccountJob.AccountJobBuilder jobBuilder = AccountJob.builder()
                .id(UUID.randomUUID())
                .filename(curTimeMs + fileExtension)
                .status(Constants.Status.PRELOADED.name())
                .submittedBy(UserUtils.getAuthenticatedUser());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));

        if(Optional.ofNullable(workbook.getSheet("metadata"))
            .map(meta -> meta.getRow(0))
            .map(row -> row.getCell(1))
            .map(XSSFCell::getStringCellValue)
                .filter(str -> str.equalsIgnoreCase("ExcelExtract"))
                .isPresent()) {
            return submitExcelExtractTask(workbook, jobBuilder);
        } else {
            return preloadAccountRpt(workbook, jobBuilder);
        }
    }

    public AccountJob submitAccountRpt(final AccountJob job) throws IOException {
        AccountJob accountJob = job.toBuilder()
                .status(Constants.Status.PENDING.name())
                .submittedBy(UserUtils.getAuthenticatedUser())
                .generationTime(LocalDateTime.now())
                .build();
        try {
            String inputFileName = accountJob.getFilename();
            if(!tempStorage.hasFile(inputFileName)) {
                log.warn("File not present: " + inputFileName);
                accountJob.setStatus(Constants.Status.FAILED.name());
                statisticsService.updateTask(accountJob);
                return accountJob;
            }
            statisticsService.updateTask(accountJob);
            tasksService.submitTask(accountJob, Constants.GEN_QUEUE_ENDPOINT);
        } catch (Exception e) {
            log.error("Error in startGeneration", e);
            accountJob.setStatus(Constants.Status.FAILED.name());
            statisticsService.updateTask(accountJob);
        }
        return accountJob;
    }

    private AccountJob preloadAccountRpt(final XSSFWorkbook workbook,
                                            final AccountJob.AccountJobBuilder jobBuilder) throws IOException {
        AccountJob job = jobBuilder
                .company(ExcelUtils.extractCompanyName(workbook, "Control", "D2"))
                .period(ExcelUtils.extractPeriodEnding(workbook, "Control", "D12"))
                .build();
        statisticsService.updateTask(job);
        return job;
    }


    private AccountJob submitExcelExtractTask(final XSSFWorkbook workbook,
            final AccountJob.AccountJobBuilder jobBuilder) throws IOException {
        AccountJob job = jobBuilder
                .company(ExcelUtils.extractCompanyName(workbook, "Control", "B1"))
                .period(ExcelUtils.extractPeriodEnding(workbook, "Control", "B11"))
                .status(Constants.Status.PENDING.name())
                .generationTime(LocalDateTime.now())
                .build();
        statisticsService.updateTask(job);
        tasksService.submitTask(job, Constants.GEN_QUEUE_ENDPOINT_EXECL_EXTRACT);
        return job;
    }



}