package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.StatisticDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    /**
     * Gets generation counts by year and month
     * @return map of yyyyMM, generation count
     */
    Map<String, StatisticDto> getGenerationStatistic();

    List<AccountFileDto> getRecentGenerations();

    List<AccountFileDto> getGenerationsByYearMonth(String yyyyMM);

    void updateAccountReport(AccountFileDto dto) throws IOException;

}
