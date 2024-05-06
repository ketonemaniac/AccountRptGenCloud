package net.ketone.accrptgen.app.service.tasks;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.util.ExcelUtils;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.app.util.UserUtils;
import net.ketone.accrptgen.task.AccountRptTask;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Sinks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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

    public Flux<ServerSentEvent<AccountJob>> triage(final String fileExtension, final byte[] fileBytes,
                                                    final Optional<User> optionalUser, final Integer clientRandInt) throws IOException {
        final Sinks.Many<ServerSentEvent<AccountJob>> sink = Sinks.many().unicast().onBackpressureError();
        long curTimeMs = System.currentTimeMillis();
        tempStorage.store(fileBytes, curTimeMs + fileExtension);
        AccountJob.AccountJobBuilder jobBuilder = AccountJob.builder()
                .id(UUID.randomUUID())
                .filename(curTimeMs + fileExtension)
                .submittedBy(UserUtils.getAuthenticatedUser())
                .noCCemail(optionalUser.map(User::getNoCCemail).orElse(Boolean.FALSE))
                .clientRandInt(clientRandInt);
        sink.tryEmitNext(toSSE(jobBuilder.status(Constants.Status.PRELOADED.name()).build()));
        new Thread(() -> Try.run(() -> {
            XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(fileBytes));

            if(Optional.ofNullable(workbook.getSheet("metadata"))
                    .map(meta -> meta.getRow(0))
                    .map(row -> row.getCell(1))
                    .map(XSSFCell::getStringCellValue)
                    .filter(str -> str.equalsIgnoreCase(Constants.DOCTYPE_EXCEL_EXTRACT))
                    .isPresent()) {
                jobBuilder.docType(Constants.DOCTYPE_EXCEL_EXTRACT);
                submitExcelExtractTask(sink, workbook, jobBuilder);
            } else {
                jobBuilder.docType(Constants.DOCTYPE_ACCOUNT_RPT);
                submitAccountRpt(sink, workbook, jobBuilder);
            }
        }).getOrElseThrow(this::handleError)).start();
        return sink.asFlux();
    }

    public void submitAccountRpt(final Sinks.Many<ServerSentEvent<AccountJob>> sink,
                                       final XSSFWorkbook workbook,
                                       final AccountJob.AccountJobBuilder jobBuilder) throws IOException {
        AccountJob accountJob = jobBuilder
                .company(ExcelUtils.extractByTitleCellName(workbook, "Control", "Company's name", 3))
                .period(ExcelUtils.extractByTitleCellName(workbook, "Control", "To", 3).substring(0, 6))
                .auditorName(ExcelUtils.extractByTitleCellName(workbook, "Control", "Auditor's name", 3))
                .referredBy(ExcelUtils.extractByTitleCellName(workbook, "Control", "Referrer", 3))
                .inCharge(ExcelUtils.extractByTitleCellName(workbook, "Control", "In-Charge", 3))
                .status(Constants.Status.PENDING.name())
                .generationTime(LocalDateTime.now())
                .firstYear(Double.parseDouble(
                        ExcelUtils.extractByTitleCellName(workbook, "Control", "First audit?", 3))
                        == 1.0)
                .build();
        sink.tryEmitNext(toSSE(accountJob));
        statisticsService.updateTask(accountJob);
        tasksService.submitTask(accountJob, Constants.GEN_QUEUE_ENDPOINT, sink);
    }

    private void submitExcelExtractTask(final Sinks.Many<ServerSentEvent<AccountJob>> sink,
            final XSSFWorkbook workbook,
            final AccountJob.AccountJobBuilder jobBuilder) throws IOException {
        AccountJob job = jobBuilder
                .company(ExcelUtils.extractByTitleCellName(workbook, "Control", "Company name", 1))
                .period(ExcelUtils.extractByTitleCellName(workbook, "Control", "Period from", 1).substring(0, 6))
                .auditorName(ExcelUtils.extractByTitleCellName(workbook, "Control", "Auditor's name",1))
                .referredBy(ExcelUtils.extractByTitleCellName(workbook, "Control", "Referrer", 1))
                .fundingType(ExcelUtils.extractByTitleCellName(workbook, "Control",
                        "Funding type (D-biz, TVP, Bud)", 1))
                .inCharge(ExcelUtils.extractByTitleCellName(workbook, "Control", "In-Charge", 1))
                .status(Constants.Status.PENDING.name())
                .generationTime(LocalDateTime.now())
                .build();
        statisticsService.updateTask(job);
        tasksService.submitTask(job, Constants.GEN_QUEUE_ENDPOINT_EXECL_EXTRACT, sink);
    }

    private RuntimeException handleError(final Throwable e) {
        log.error("Error in startGeneration", e);
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

}
