package net.ketone.accrptgen.threading;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
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
public class GCloudStandardThreadingService implements ThreadingService {

    private static final Logger logger = Logger.getLogger(GCloudStandardThreadingService.class.getName());
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    @Qualifier("fileBasedStatisticsService")
    private StatisticsService fileBasedStatisticsService;

    @Autowired
    private ApplicationContext ctx;

    @Override
    public AccountFileDto runPipeline(Date generationTime) throws IOException {
        // Queue queue = QueueFactory.getDefaultQueue();
        Queue queue = QueueFactory.getQueue("accountrptgen-queue");
//        String genTimeStr = GenerationService.sdf.format(generationTime);
        TaskHandle handle = queue.add(TaskOptions.Builder.withUrl("/worker")
//                .param("companyName", companyName)
                .param("generationTime", ""+generationTime.getTime())
//                .param("filename", filename)
        );
        logger.info("Handle Created: " + handle.getName());

        AccountFileDto dto = new AccountFileDto();
//        dto.setCompany(companyName);
//        dto.setFilename(filename);
        dto.setGenerationTime(generationTime);
        dto.setStatus(AccountFileDto.Status.PENDING.name());
        dto.setHandleName(handle.getName());
        statisticsService.updateAccountReport(dto);
        return dto;
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
        statisticsService.updateAccountReport(dto);
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto.getGenerationTime());
        pipeline.run();
        return "OK";
    }

    @PostMapping("/updateStat")
    public String doSaveStat(@RequestParam("dto") String dto) throws IOException {
        logger.info("Updating statistics for " + dto);
        fileBasedStatisticsService.updateAccountReport(mapper.readValue(dto, AccountFileDto.class));
        return "OK";
    }

}
