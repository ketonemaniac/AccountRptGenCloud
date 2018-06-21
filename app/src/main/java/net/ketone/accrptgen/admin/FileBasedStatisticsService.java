package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.dto.StatisticDto;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class FileBasedStatisticsService implements StatisticsService {

    private static final Logger logger = Logger.getLogger(FileBasedStatisticsService.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();

    // statistics by month
    public static String STATS_FILE = "statistics.txt";
    // all history, latest first
    public static String HISTORY_FILE = "history.txt";
    public static int MAX_RECENTS = 10;

    @Autowired
    private StorageService storageService;


    @Override
    public Map<String, StatisticDto> getGenerationStatistic() {
        return null;
    }

    @Override
    public List<AccountFileDto> getRecentGenerations() {
        // TODO: read from cache instead of file
        try {
            Deque<AccountFileDto> lines = loadHistoryFileToDeque();
            return lines.stream().limit(10).collect(Collectors.toList());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot read from " + HISTORY_FILE, e);
        }
        return null;
    }

    @Override
    public List<AccountFileDto> getGenerationsByYearMonth(String yyyyMM) {
        return null;
    }

    @Override
    public void updateAccountReport(AccountFileDto dto) throws IOException {

        Deque<AccountFileDto> lines = loadHistoryFileToDeque();

        // TODO: determine from cache instead of file
        // check whether this is the update of an existing line
        boolean isUpdate = false;
        for(AccountFileDto lineDto : lines) {
            if(lineDto.getFilename().equals(dto.getFilename())) {
                lineDto.setStatus(dto.getStatus());
                isUpdate = true;
            }
        }
        if(!isUpdate) {
            lines.offerFirst(dto);
        }

        writeHistoryFileFromQueue(lines);
    }

    private Deque<AccountFileDto> loadHistoryFileToDeque() throws IOException {
        InputStream is = storageService.load(HISTORY_FILE);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        Deque<AccountFileDto> lines = buf.lines()
                .map(line -> {
                    try {
                        return objectMapper.readValue(line, AccountFileDto.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new AccountFileDto();
                })
                .collect(Collectors.toCollection(ArrayDeque::new));
        is.close();
        return lines;
    }

    private void writeHistoryFileFromQueue(Queue<AccountFileDto> dtos) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(AccountFileDto lineDto : dtos) {
            sb.append(objectMapper.writeValueAsString(lineDto)).append(System.lineSeparator());
        }
        InputStream output = new ByteArrayInputStream(sb.toString().getBytes());
        storageService.store(output, HISTORY_FILE);
        output.close();
    }

}
