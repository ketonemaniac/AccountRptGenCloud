package net.ketone.accrptgen.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.dto.AccountFileDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.cache.Cache;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.config.Constants.STATUS_QUEUE_ENDPOINT;
import static net.ketone.accrptgen.config.Constants.STATUS_QUEUE_NAME;

@Service
@Profile({"gCloudStandard","gCloudFlexible"})
@Primary
@RestController
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
    public List<AccountFileDto> getRecentTasks() {
        TreeMap<String, AccountFileDto> recents = (TreeMap<String, AccountFileDto>)
                cache.get(RECENTS);
        if(recents == null) {
            // default getting from service
            logger.info("Populating memcache...");
            recents = fileBasedStatisticsService.getRecentTasks().stream().collect(
                    Collectors.toMap(dto -> ""+dto.getGenerationTime().getTime(),
                            Function.identity(),
                            (v1,v2) -> v1,
                            TreeMap::new));
            cache.put(RECENTS, recents);
        }
        return new ArrayList<>(recents.descendingMap().values());
    }

    @Override
    public Map<String, Integer> housekeepTasks() throws IOException {
        // clean cache because housekeeping is underway
        cache.clear();
        // just give the responsibility to the file
        return fileBasedStatisticsService.housekeepTasks();
    }

    @Override
    public void updateTask(AccountFileDto dto) throws IOException {
        logger.info("update task company=" + dto.getCompany() + " status=" + dto.getStatus()
         + " generationTime=" + dto.getGenerationTime());
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
        if(Constants.Status.EMAIL_SENT.name().equals(dto.getStatus()) ||
            Constants.Status.FAILED.name().equals(dto.getStatus())) {
            logger.info("queuing status=" + dto.getStatus());
            // only put TERMINAL status to file
            Queue queue = QueueFactory.getQueue(STATUS_QUEUE_NAME);
            TaskOptions task = TaskOptions.Builder
                    .withMethod(TaskOptions.Method.POST)
                    .url(STATUS_QUEUE_ENDPOINT)
                    .param("dto", mapper.writeValueAsString(recents.get(key)));
            queue.add(task);
        }
        // update cache
        cache.put(RECENTS, recents);
        cache.put(dto.getHandleName(), existingDto == null ? dto : existingDto);
    }

    @Override
    public AccountFileDto getTask(String handleName) {
        if(cache.containsKey(handleName)) {
            return (AccountFileDto) cache.get(handleName);
        }
        return null;

    }

    /**
     * Update file asynchronously
     * @param dto
     * @return
     * @throws IOException
     */
    @PostMapping(STATUS_QUEUE_ENDPOINT)
    public String doSaveStat(@RequestParam("dto") String dto) throws IOException {
        logger.info("Updating statistics for " + dto);
        fileBasedStatisticsService.updateTask(mapper.readValue(dto, AccountFileDto.class));
        return "OK";
    }

}

