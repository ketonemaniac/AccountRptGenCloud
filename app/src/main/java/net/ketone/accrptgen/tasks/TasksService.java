package net.ketone.accrptgen.tasks;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.Date;

public interface TasksService {

    AccountFileDto submitTask(Date generationTime) throws IOException;

    boolean terminateTask(String task);
}
