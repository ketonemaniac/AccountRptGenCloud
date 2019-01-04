package net.ketone.accrptgen.threading;

import com.google.appengine.api.taskqueue.TaskHandle;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.channels.Pipe;
import java.util.Date;

@Component
@Profile({"local","gCloudFlexible"})
public class SimpleThreadingService implements ThreadingService {

    @Autowired
    private ApplicationContext ctx;


    @Override
    public AccountFileDto runPipeline(Date generationTime) throws IOException {
        Pipeline pipeline = ctx.getBean(Pipeline.class, generationTime);
        new Thread(pipeline).start();
        return null;    // there is nothing you could stop the thread
    }
}
