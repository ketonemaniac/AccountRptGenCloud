package net.ketone.accrptgen.tasks;

import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.gen.GenerationService;
import net.ketone.accrptgen.gen.Pipeline;
import net.ketone.accrptgen.stats.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;

@Component
@Profile({"local","gCloudFlexible"})
public class SimpleTasksService implements TasksService {

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private StatisticsService statisticsService;


    @Override
    public AccountFileDto submitTask(AccountFileDto dto) throws IOException {


        Pipeline pipeline = ctx.getBean(Pipeline.class, dto);
        new Thread(pipeline).start();

        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
