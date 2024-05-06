package net.ketone.accrptgen.app.service;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ClientRandIntChecker {

    @Autowired
    private StatisticsService statisticsService;

    public boolean checkDuplicate(Integer clientRandInt) {
        if(statisticsService.getTaskByClientRandInt(clientRandInt) != null) {
            log.info("duplicate request to server!!! {}", clientRandInt);
            return true;
        }
        log.info("not duplicate request {}", clientRandInt);
        return false;
    }

}
