package net.ketone.accrptgen.app.service.tasks;

import net.ketone.accrptgen.common.model.AccountJob;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Sinks;

import java.io.IOException;

public interface TasksService {

    void submitTask(AccountJob dto, String endpoint, final Sinks.Many<ServerSentEvent<AccountJob>> sink) throws IOException;

    boolean terminateTask(String task);
}
