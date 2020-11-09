package net.ketone.accrptgen.service.stats;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    int MAX_RECENTS = 10;

    List<AccountFileDto> getRecentTasks(String authenticatedUser);

    /**
     *
     * @return Map of filename: count of lines after housekeep
     */
    Map<String, Integer> housekeepTasks() throws IOException;

    void updateTask(AccountFileDto dto) throws IOException;

    AccountFileDto getTask(String task);
}
