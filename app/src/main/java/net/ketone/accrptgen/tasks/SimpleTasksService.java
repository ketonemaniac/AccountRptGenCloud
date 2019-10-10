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
    public AccountFileDto submitTask(String cacheFilename, String company, String referredBy) throws IOException {

        AccountFileDto dto = new AccountFileDto();
        dto.setGenerationTime(new Date(Long.parseLong(cacheFilename)));
        dto.setFilename(GenerationService.getFileName(company, dto.getGenerationTime()));
        dto.setCompany(company);
        dto.setStatus(Constants.Status.GENERATING.name());
        statisticsService.updateTask(dto);

        Pipeline pipeline = ctx.getBean(Pipeline.class, cacheFilename, dto.getGenerationTime(), company);
        new Thread(pipeline).start();

        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
