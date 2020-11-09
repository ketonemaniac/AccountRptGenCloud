package net.ketone.accrptgen.service.tasks;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;

public interface TasksService {

    AccountFileDto submitTask(AccountFileDto dto) throws IOException;

    boolean terminateTask(String task);
}
