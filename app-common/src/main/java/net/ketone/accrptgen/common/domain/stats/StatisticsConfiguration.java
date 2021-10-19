package net.ketone.accrptgen.common.domain.stats;

import net.ketone.accrptgen.common.repo.AccountJobRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsConfiguration {

    @Value("${storage.temp.retention.days}")
    private int retentionDays;

    @Bean
    public StatisticsService statisticsService(final AccountJobRepository accountJobRepository) {
        return new MongoStatisticsService(accountJobRepository, retentionDays);
    }

}
