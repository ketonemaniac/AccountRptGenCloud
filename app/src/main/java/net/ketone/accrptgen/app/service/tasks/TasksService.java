package net.ketone.accrptgen.app.service.tasks;

import net.ketone.accrptgen.common.model.AccountJob;

import java.io.IOException;

public interface TasksService {

    AccountJob submitTask(AccountJob dto, String endpoint) throws IOException;

    boolean terminateTask(String task);
}
