package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.StatisticDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.cache.Cache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Profile("!local")
@Primary
public class MemcacheStatisticsService implements StatisticsService {

    private static final Logger logger = Logger.getLogger(MemcacheStatisticsService.class.getName());
    private static final String RECENTS = "Recents";
    private ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private Cache cache;

    @Autowired
    @Qualifier("fileBasedStatisticsService")
    private StatisticsService fileBasedStatisticsService;

    @Override
    public Map<String, StatisticDto> getGenerationStatistic() {
        return null;
    }

    @Override
    public List<AccountFileDto> getRecentGenerations() {
        TreeMap<String, AccountFileDto> recents = (TreeMap<String, AccountFileDto>)
                cache.get(RECENTS);
        if(recents == null) {
            // default getting from service
            recents = fileBasedStatisticsService.getRecentGenerations().stream().collect(
                    Collectors.toMap(dto -> ""+dto.getGenerationTime().getTime(),
                            Function.identity(),
                            (v1,v2) -> v1,
                            TreeMap::new));
            cache.put(RECENTS, recents);
        }
        logger.info("Recent length " + recents.size());
        return new ArrayList<>(recents.descendingMap().values());
    }

    @Override
    public void updateAccountReport(AccountFileDto dto) throws IOException {
        String key = ""+dto.getGenerationTime().getTime();
        TreeMap<String, AccountFileDto> recents =(TreeMap<String, AccountFileDto>)
                cache.getOrDefault(RECENTS, new TreeMap<String, AccountFileDto>());

        AccountFileDto existingDto = recents.get(key);
        if(existingDto == null) {
            recents.put(key, dto);
            logger.info("putting " + key);
            if(recents.size() > StatisticsService.MAX_RECENTS) {
                recents.remove(recents.firstKey());
            }
        } else {
            // update fields
            existingDto.setFilename(dto.getFilename());
            existingDto.setCompany(dto.getCompany());
            existingDto.setStatus(dto.getStatus());
        }
        Queue queue = QueueFactory.getQueue("statistics-queue");
        TaskOptions task = TaskOptions.Builder
                .withMethod(TaskOptions.Method.POST)
                .url("/updateStat")
                .param("dto", mapper.writeValueAsString(recents.get(key)));
                // .payload(mapper.writeValueAsString(recents.get(key)))
//                .method(TaskOptions.Method.POST);
//        task.header("Content-type", "application/json");
        queue.add(task);
        cache.put(RECENTS, recents);
    }

    @Override
    public AccountFileDto getAccountReport(String handleName) {
        if(cache.containsKey(handleName)) {
            return (AccountFileDto) cache.get(handleName);
        }
        return null;

    }

}
