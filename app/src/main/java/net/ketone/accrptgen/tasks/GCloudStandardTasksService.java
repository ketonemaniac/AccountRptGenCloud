package net.ketone.accrptgen.tasks;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.stats.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Works for Google cloud Standard environment
 */
@Profile("gCloudStandard")
@RestController
public class GCloudStandardTasksService implements TasksService {

    private static final Logger logger = Logger.getLogger(GCloudStandardTasksService.class.getName());
    private static final String QUEUE_NAME= "accountrptgen-queue";

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    @Qualifier("fileBasedStatisticsService")
    private StatisticsService fileBasedStatisticsService;

    @Autowired
    private ApplicationContext ctx;

    @Override
    public AccountFileDto submitTask(Date generationTime) throws IOException {
        Queue queue = QueueFactory.getQueue(QUEUE_NAME);
        TaskHandle handle = queue.add(TaskOptions.Builder.withUrl("/worker")
                .param("generationTime", ""+generationTime.getTime())
        );
        logger.info("Handle Created: " + handle.getName());

        AccountFileDto dto = new AccountFileDto();
        dto.setGenerationTime(generationTime);
        dto.setStatus(AccountFileDto.Status.PENDING.name());
        dto.setHandleName(handle.getName());
        statisticsService.updateTask(dto);
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        Queue q = QueueFactory.getQueue(QUEUE_NAME);
        return q.deleteTask(task);
    }

    @PostMapping("/worker")
    public String doWork(// @RequestParam("companyName") String companyName,
                         @RequestParam("generationTime") String generationTime
                         // @RequestParam("filename") String filename
                         ) throws ParseException, IOException {

//        Date genTime = GenerationService.sdf.parse(generationTime);
        AccountFileDto dto = new AccountFileDto();
//        dto.setCompany(companyName);
//        dto.setFilename(filename);
        dto.setGenerationTime(new Date(Long.parseLong(generationTime)));
        dto.setStatus(AccountFileDto.Status.GENERATING.name());
//        logger.info("Updating statistics for " + filename);
        statisticsService.updateTask(dto);
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto.getGenerationTime());
        pipeline.run();
        return "OK";
    }

}
