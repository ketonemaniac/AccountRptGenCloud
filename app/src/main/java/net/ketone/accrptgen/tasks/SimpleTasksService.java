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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Profile({"local","gCloudFlexible"})
public class SimpleTasksService implements TasksService {

    private static final Logger logger = Logger.getLogger(SimpleTasksService.class.getName());

    ExecutorService executor = Executors.newFixedThreadPool(1);
    List<Future> tasks = new ArrayList<>();

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private StatisticsService statisticsService;


    @Override
    public AccountFileDto submitTask(AccountFileDto dto) throws IOException {
        dto.setStatus(Constants.Status.GENERATING.name());
        statisticsService.updateTask(dto);
        Pipeline pipeline = ctx.getBean(Pipeline.class, dto);
        tasks.add(executor.submit(pipeline));
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
