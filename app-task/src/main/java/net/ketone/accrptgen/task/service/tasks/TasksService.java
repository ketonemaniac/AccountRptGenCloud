package net.ketone.accrptgen.task.service.tasks;

import net.ketone.accrptgen.common.model.AccountJob;

import java.io.IOException;

public interface TasksService {

    void submitTask(AccountJob dto, String endpoint) throws IOException;

    boolean terminateTask(String task);
}
