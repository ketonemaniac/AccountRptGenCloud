package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Integer> getGenerationCounts();

    List<AccountFileDto> getRecentGenerations();

    void updateAccountReport(AccountFileDto dto) throws IOException;

}
