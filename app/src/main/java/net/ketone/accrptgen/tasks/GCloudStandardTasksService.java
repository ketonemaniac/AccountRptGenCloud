package net.ketone.accrptgen.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.stats.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.ketone.accrptgen.config.Constants.GEN_QUEUE_NAME;
import static net.ketone.accrptgen.config.Constants.GEN_QUEUE_ENDPOINT;

/**
 * Works for Google cloud Standard environment
 */
@Profile("gCloudStandard")
@RestController
public class GCloudStandardTasksService implements TasksService {

    private static final Logger logger = Logger.getLogger(GCloudStandardTasksService.class.getName());

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    @Qualifier("fileBasedStatisticsService")
    private StatisticsService fileBasedStatisticsService;

    @Autowired
    private ApplicationContext ctx;

    @Override
    public AccountFileDto submitTask(String cacheFilename, String company, String referredBy) throws IOException {
        Queue queue = QueueFactory.getQueue(GEN_QUEUE_NAME);
//        Queue queue = QueueFactory.getDefaultQueue();
        TaskHandle handle = queue.add(TaskOptions.Builder.withUrl(GEN_QUEUE_ENDPOINT)
                .param("companyName", company)
                .param("generationTime", cacheFilename)
        );
        logger.info("Handle Created: " + handle.getName());

        AccountFileDto dto = new AccountFileDto();
        dto.setGenerationTime(new Date(Long.parseLong(cacheFilename)));
        dto.setFilename(GenerationService.getFileName(company, dto.getGenerationTime()));
        dto.setCompany(company);
        dto.setStatus(Constants.Status.PENDING.name());
        dto.setHandleName(handle.getName());
        statisticsService.updateTask(dto);
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        Queue q = QueueFactory.getQueue(GEN_QUEUE_NAME);
        return q.deleteTask(task);
    }

    @PostMapping(GEN_QUEUE_ENDPOINT)
    public String doWork(@RequestParam("companyName") String companyName,
                         @RequestParam("generationTime") String generationTime
                         ) {

        AccountFileDto dto = new AccountFileDto();
        dto.setCompany(companyName);
        dto.setFilename(generationTime);
        dto.setGenerationTime(new Date(Long.parseLong(generationTime)));
        dto.setStatus(Constants.Status.GENERATING.name());
        try {
            statisticsService.updateTask(dto);
        } catch (IOException e) {
            logger.log(Level.WARNING, "History file write failed (GENERATING)", e);
            return "NOT OK";
        }
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto.getFilename(), dto.getGenerationTime(), companyName);
        pipeline.run();
        return "OK";
    }

}
