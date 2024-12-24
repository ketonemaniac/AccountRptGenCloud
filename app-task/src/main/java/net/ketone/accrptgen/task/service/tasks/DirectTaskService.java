package net.ketone.accrptgen.task.service.tasks;

import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.task.AccountRptTask;
import net.ketone.accrptgen.task.ExcelExtractTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Sinks;

import java.io.IOException;

@Component
@Profile({"local","cloudRun"})
public class DirectTaskService implements TasksService {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public void submitTask(AccountJob dto, String endpoint, final Sinks.Many<ServerSentEvent<AccountJob>> sink) throws IOException {
        Runnable task = null;
        switch(endpoint) {
            case Constants.GEN_QUEUE_ENDPOINT:
                task = ctx.getBean(AccountRptTask.class, dto, sink); break;
            case Constants.GEN_QUEUE_ENDPOINT_EXECL_EXTRACT:
                task = ctx.getBean(ExcelExtractTask.class, dto, sink); break;
        }
        if(task != null) {
            task.run();
        }
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
