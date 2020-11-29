package net.ketone.accrptgen.service.stats;

import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.repo.AccountJobRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
public class MongoStatisticsService implements StatisticsService {

    @Autowired
    private AccountJobRepository repository;

    @Value("${storage.temp.retention.days}")
    private int retentionDays;

    @Override
    public List<AccountJob> getRecentTasks(String authenticatedUser) {
        return repository.findTop10ByOrderByGenerationTimeDesc()
                .stream()
                .filter(job -> job.getGenerationTime().isAfter(LocalDateTime.now()
                        .minus(retentionDays, ChronoUnit.DAYS)))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> housekeepTasks() throws IOException {
        throw new NotImplementedException("Not Implemented");
    }

    @Override
    public void updateTask(AccountJob dto) throws IOException {
        repository.save(dto);
    }

    @Override
    public AccountJob getTask(String task) {
        return null;
    }
}
