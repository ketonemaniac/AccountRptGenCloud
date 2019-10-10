package net.ketone.accrptgen.tasks;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.Date;

public interface TasksService {

    AccountFileDto submitTask(String cacheFilename, String company, String referredBy) throws IOException;

    boolean terminateTask(String task);
}
