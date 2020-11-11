package net.ketone.accrptgen.service.tasks;

import net.ketone.accrptgen.domain.dto.AccountJob;

import java.io.IOException;

public interface TasksService {

    AccountJob submitTask(AccountJob dto) throws IOException;

    boolean terminateTask(String task);
}
