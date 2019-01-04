package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.StatisticDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    int MAX_RECENTS = 10;
    /**
     * Gets generation counts by year and month
     * @return map of yyyyMM, generation count
     */
    Map<String, StatisticDto> getGenerationStatistic();

    List<AccountFileDto> getRecentGenerations();

    void updateAccountReport(AccountFileDto dto) throws IOException;

    AccountFileDto getAccountReport(String handleName);
}
