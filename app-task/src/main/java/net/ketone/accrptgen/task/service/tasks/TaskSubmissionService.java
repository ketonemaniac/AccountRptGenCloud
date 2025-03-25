package net.ketone.accrptgen.task.service.tasks;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.util.ExcelCellUtils;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.util.FileUtils;
import net.ketone.accrptgen.task.GenerateAFSTask;
import net.ketone.accrptgen.task.GenerateTabTask;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static net.ketone.accrptgen.common.util.SSEUtils.toSSE;

@Slf4j
@Component
public class TaskSubmissionService {

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private StorageService tempStorage;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private GenerateTabTask generateTabTask;
    @Autowired
    private GenerateAFSTask generateAFSTask;

    public void triage(String docType, final String fileExtension, final byte[] fileBytes,
                       final Optional<User> optionalUser, final Integer clientRandInt) throws Exception {
        tempStorage.store(fileBytes, clientRandInt + fileExtension);
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));
        LocalDateTime generationTime = ZonedDateTime.now(ZoneId.of("UTC+8")).toLocalDateTime();
        AccountJob.AccountJobBuilder accountJobBuilder = AccountJob.builder()
                .id(UUID.randomUUID())
                .submittedBy(optionalUser.map(User::getUsername).orElse("Anonymous"))
                .noCCemail(optionalUser.map(User::getNoCCemail).orElse(Boolean.FALSE))
                .clientRandInt(clientRandInt)
                .docType(docType)
                .status(Constants.Status.GENERATING.name())
                .generationTime(generationTime);
        AccountJob accountJob = null;

        if(Optional.ofNullable(workbook.getSheet("metadata"))
                .map(meta -> meta.getRow(0))
                .map(row -> row.getCell(1))
                .map(XSSFCell::getStringCellValue)
                .filter(str -> str.equalsIgnoreCase(Constants.DOCTYPE_EXCEL_EXTRACT))
                .isPresent()) {
            docType = Constants.DOCTYPE_EXCEL_EXTRACT;
        }

        switch(docType) {
            case Constants.DOCTYPE_BREAKDOWN_TABS:
                accountJob = accountJobForBreakdownAndAFS(accountJobBuilder, workbook, (company, period) ->
                        FileUtils.uniqueFilename(company, generationTime) + "-" + period + " - working"  + fileExtension);
                generateTabTask.run(accountJob, fileBytes);
                break;
            case Constants.DOCTYPE_GENERATE_AFS:
                accountJob = accountJobForBreakdownAndAFS(accountJobBuilder, workbook, (company, period) ->
                        FileUtils.uniqueFilename(company, generationTime) + "-" + period + " - AFS & Tax"   + fileExtension);
                generateAFSTask.run(accountJob, fileBytes);
                break;
            case Constants.DOCTYPE_EXCEL_EXTRACT:
                submitExcelExtractTask(workbook, accountJobBuilder, (company) ->
                        FileUtils.uniqueFilename(company, generationTime) + fileExtension);
                break;
            case Constants.DOCTYPE_ACCOUNT_RPT:
                submitAccountRpt(workbook, accountJobBuilder, (company) ->
                        FileUtils.uniqueFilename(company, generationTime) + fileExtension);
                break;
        }

    }

    private AccountJob accountJobForBreakdownAndAFS(AccountJob.AccountJobBuilder accountJobBuilder, XSSFWorkbook workbook,
                                                    BiFunction<String, String, String> fileNameFn) throws IOException {
        String company = ExcelCellUtils.extractByTitleCellName(workbook, "Content", "Company Name", 1);
        String period = ExcelCellUtils.extractByTitleCellName(workbook, "Content", "Current Year/ period", 1).substring(0, 6);
        AccountJob accountJob = accountJobBuilder.filename(fileNameFn.apply(company, period))
                .company(company)
                .period(period)
                .build();
        statisticsService.updateTask(accountJob);
        return accountJob;
    }

    public void submitAccountRpt(final XSSFWorkbook workbook,
                                       final AccountJob.AccountJobBuilder jobBuilder, Function<String, String> fileNameFn) throws IOException {
        String company = ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Company's name", 3);
        AccountJob accountJob = jobBuilder
                .company(company)
                .period(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "To", 3).substring(0, 6))
                .auditorName(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Auditor's name", 3))
                .referredBy(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Referrer", 3))
                .inCharge(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "In-Charge", 3))
                .status(Constants.Status.PENDING.name())
                .filename(fileNameFn.apply(company))
                .firstYear(Double.parseDouble(
                        ExcelCellUtils.extractByTitleCellName(workbook, "Control", "First audit?", 3))
                        == 1.0)
                .build();
        statisticsService.updateTask(accountJob);
        tasksService.submitTask(accountJob, Constants.GEN_QUEUE_ENDPOINT);
    }

    private void submitExcelExtractTask(final XSSFWorkbook workbook,
            final AccountJob.AccountJobBuilder jobBuilder, Function<String, String> fileNameFn) throws IOException {
        String company = ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Company name", 1);
        AccountJob job = jobBuilder
                .company(company)
                .period(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Period from", 1).substring(0, 6))
                .auditorName(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Auditor's name",1))
                .referredBy(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "Referrer", 1))
                .fundingType(ExcelCellUtils.extractByTitleCellName(workbook, "Control",
                        "Funding type (D-biz, TVP, Bud)", 1))
                .inCharge(ExcelCellUtils.extractByTitleCellName(workbook, "Control", "In-Charge", 1))
                .status(Constants.Status.PENDING.name())
                .filename(fileNameFn.apply(company))
                .build();
        statisticsService.updateTask(job);
        tasksService.submitTask(job, Constants.GEN_QUEUE_ENDPOINT_EXECL_EXTRACT);
    }

    private RuntimeException handleError(final Throwable e) {
        log.error("Error in startGeneration", e);
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

}
