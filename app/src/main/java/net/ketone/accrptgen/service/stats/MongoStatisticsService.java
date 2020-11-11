package net.ketone.accrptgen.service.stats;

import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.repo.AccountJobRepository;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
@Primary
public class MongoStatisticsService implements StatisticsService {

    @Autowired
    private AccountJobRepository repository;

    @Override
    public List<AccountJob> getRecentTasks(String authenticatedUser) {
        return repository.findTop10ByOrderByGenerationTimeDesc();
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
