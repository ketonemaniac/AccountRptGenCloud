package net.ketone.accrptgen.common.domain.stats;

import lombok.RequiredArgsConstructor;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.repo.AccountJobRepository;
import org.apache.commons.lang3.NotImplementedException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class MongoStatisticsService implements StatisticsService {

    private final AccountJobRepository repository;

    private final int retentionDays;

    @Override
    public List<AccountJob> getRecentTasks(final String authenticatedUser, final String docType) {
        return repository.findTop10BySubmittedByAndDocTypeOrderByGenerationTimeDesc(authenticatedUser, docType)
                .stream()
//                .filter(job -> job.getGenerationTime().isAfter(LocalDateTime.now()
//                        .minus(retentionDays, ChronoUnit.DAYS)))
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
    public AccountJob getTaskByClientRandInt(Integer clientRandInt) {
        return repository.getByClientRandInt(clientRandInt);
    }
}
