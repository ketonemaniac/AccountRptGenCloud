package net.ketone.accrptgen.app.service.tasks;

import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.task.AccountRptTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Profile({"local","cloudRun"})
public class DirectTaskService implements TasksService {

    @Autowired
    private ApplicationContext ctx;

    @Override
    public AccountJob submitTask(AccountJob dto, String endpoint) throws IOException {
        AccountRptTask accountRptTask = ctx.getBean(AccountRptTask.class, dto);
        accountRptTask.run();
        dto.setStatus(Constants.Status.EMAIL_SENT.name());
        return dto;
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
