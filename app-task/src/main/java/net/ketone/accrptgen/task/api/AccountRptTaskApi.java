package net.ketone.accrptgen.task.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.task.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static net.ketone.accrptgen.common.constants.Constants.GEN_QUEUE_ENDPOINT;

/**
 * Works for Google cloud Standard environment
 */
@Slf4j
@RestController
public class AccountRptTaskApi {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private StatisticsService statisticsService;

    @Autowired
    private ApplicationContext ctx;

    @PostMapping(GEN_QUEUE_ENDPOINT)
    // public String doWork(@RequestParam("accountFileDto") AccountFileDto dto) {
    public String doWork(@RequestBody AccountJob dto) {
        log.info("inside doWork() " + dto.toString());
        dto.setStatus(Constants.Status.GENERATING.name());
        try {
            statisticsService.updateTask(dto);
        } catch (IOException e) {
            log.warn("History file write failed (GENERATING)", e);
            return "NOT OK";
        }
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto);
        pipeline.run();
        return "OK";
    }

}
