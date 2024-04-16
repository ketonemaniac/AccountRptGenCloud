package net.ketone.accrptgen.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.task.AccountRptTask;
import net.ketone.accrptgen.task.ExcelExtractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Sinks;

import java.io.IOException;

import static net.ketone.accrptgen.common.constants.Constants.GEN_QUEUE_ENDPOINT_EXECL_EXTRACT;

@Slf4j
@RestController
@Deprecated
public class ExcelExtractTaskApi {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private StatisticsService statisticsService;

    @PostMapping(GEN_QUEUE_ENDPOINT_EXECL_EXTRACT)
    public String doWork(@RequestBody AccountJob dto) {
        log.info("inside " + GEN_QUEUE_ENDPOINT_EXECL_EXTRACT + " " + dto.toString());
        dto.setStatus(Constants.Status.GENERATING.name());
        try {
            statisticsService.updateTask(dto);
        } catch (IOException e) {
            log.warn("History file write failed (GENERATING)", e);
            return "NOT OK";
        }
        // this is a useless sink
        final Sinks.Many<ServerSentEvent<AccountJob>> sink = Sinks.many().unicast().onBackpressureError();
        ExcelExtractTask excelExtractTask = ctx.getBean(ExcelExtractTask.class, dto, sink);
        excelExtractTask.run();
        return "OK";
    }

}
