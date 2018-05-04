package net.ketone.accrptgen.threading;

import com.google.appengine.api.ThreadManager;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.Date;
import java.util.concurrent.ThreadFactory;

/**
 * Works for Google cloud Standard environment
 */
@Profile("gcloudStandard")
@RestController
public class GCloudStandardThreadingService implements ThreadingService {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public void runPipeline(String companyName, Date generationTime, String filename) {
        Queue queue = QueueFactory.getDefaultQueue();
        String genTimeStr = GenerationService.sdf.format(generationTime);
        queue.add(TaskOptions.Builder.withUrl("/worker")
                .param("companyName", companyName)
                .param("generationTime", genTimeStr)
                .param("filename", filename));
    }


    @PostMapping("/worker")
    public String doWork(@RequestParam("companyName") String companyName,
                         @RequestParam("generationTime") String generationTime,
                         @RequestParam("filename") String filename) throws ParseException {

        Date genTime = GenerationService.sdf.parse(generationTime);
        Pipeline pipeline = ctx.getBean(Pipeline.class, companyName, genTime, filename);
        pipeline.run();
        return "OK";
    }

}
