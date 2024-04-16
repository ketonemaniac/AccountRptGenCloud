package net.ketone.accrptgen.app.service.tasks;

import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Sinks;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * for AppEngine only.
 */
@Deprecated
public class LocalTaskService implements TasksService {

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.create("http://localhost:8080");
    }


    @Override
    public void submitTask(final AccountJob dto, final String endpoint, final Sinks.Many<ServerSentEvent<AccountJob>> sink) throws IOException {
        webClient.post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
