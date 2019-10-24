package net.ketone.accrptgen.tasks;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.Date;

public interface TasksService {

    AccountFileDto submitTask(AccountFileDto dto) throws IOException;

    boolean terminateTask(String task);
}
