package net.ketone.accrptgen.threading;

import net.ketone.accrptgen.gen.Pipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.channels.Pipe;
import java.util.Date;

@Component
@Profile({"local","gCloudFlexible"})
public class SimpleThreadingService implements ThreadingService {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public void runPipeline(String companyName, Date generationTime, String filename) {
        Pipeline pipeline = ctx.getBean(Pipeline.class, companyName, generationTime, filename);
        new Thread(pipeline).start();
    }
}
