package net.ketone.accrptgen.common.domain.stats;

import net.ketone.accrptgen.common.model.AccountJob;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    List<AccountJob> getRecentTasks(String authenticatedUser);

    /**
     *
     * @return Map of filename: count of lines after housekeep
     */
    Map<String, Integer> housekeepTasks() throws IOException;

    void updateTask(AccountJob dto) throws IOException;

    AccountJob getTask(String task);
}
