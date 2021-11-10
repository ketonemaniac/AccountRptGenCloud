package net.ketone.accrptgen.app.service.tasks;

import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
@Profile({"local"})
public class LocalTaskService implements TasksService {

    ExecutorService executor = Executors.newFixedThreadPool(1);
    List<Future> tasks = new ArrayList<>();

    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private StatisticsService statisticsService;

    private WebClient webClient;

    @PostConstruct
    public void init() {
        webClient = WebClient.create("http://localhost:8080");
    }


    @Override
    public AccountJob submitTask(final AccountJob dto, final String endpoint) throws IOException {
        webClient.post()
                .uri(endpoint)
                .body(BodyInserters.fromValue(dto))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
        return AccountJob.builder().build();
    }

    @Override
    public boolean terminateTask(String task) {
        return false;
    }
}
