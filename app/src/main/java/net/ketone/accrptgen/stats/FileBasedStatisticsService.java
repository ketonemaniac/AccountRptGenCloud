package net.ketone.accrptgen.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.config.Constants.HISTORY_FILE;

@Service
public class FileBasedStatisticsService implements StatisticsService {

    private static final Logger logger = Logger.getLogger(FileBasedStatisticsService.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;
    @Autowired
    private StorageService cache;


    @Override
    public List<AccountFileDto> getRecentTasks() {
        try {
            Deque<AccountFileDto> lines = loadHistoryFileToDeque();
            return lines.stream()
                    .limit(StatisticsService.MAX_RECENTS)
                    .filter(dto -> (dto.getStatus() != null && !dto.getStatus().equals(Constants.Status.EMAIL_SENT.name()))
                            || cache.hasFile(dto.getFilename()+".zip"))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            logger.log(Level.WARNING, "Cannot read from " + HISTORY_FILE, e);
        }
        return null;
    }

    @Override
    public Map<String, Integer> housekeepTasks() throws IOException {
        Map<String, Integer> fileLineMap = new HashMap<>();
        Deque<AccountFileDto> lines = loadHistoryFileToDeque();
        String curFile = HISTORY_FILE;
        Deque<AccountFileDto> curLines = new ArrayDeque<>();
        for(AccountFileDto lineDto : lines) {
            LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
            LocalDate lineDate = new java.sql.Date(lineDto.getGenerationTime().getTime()).toLocalDate();
            if(monthStart.compareTo(lineDate) > 0) {
                // in previous month
                // deal with the cur lines first
                writeHistoryFileFromQueue(curLines);
                fileLineMap.put(curFile, curLines.size());
                curFile = getFilenameOfDate(lineDate);
                curLines = new ArrayDeque<>();
            }
            curLines.offerFirst(lineDto);
        }
        // finally, put last lines into last file
        writeHistoryFileFromQueue(curLines);
        fileLineMap.put(curFile, curLines.size());
        return fileLineMap;
    }

    private String getFilenameOfDate(LocalDate lineDate) {
        String[] historyFileStr = HISTORY_FILE.split(".");
        return historyFileStr[0] + lineDate.format(DateTimeFormatter.ofPattern("yyyyMM")) + "\\." + historyFileStr[1];
    }

    @Override
    public void updateTask(AccountFileDto dto) throws IOException {
        logger.info("dto: " + objectMapper.writeValueAsString(dto) );
        // do not store null filename entries
        if(dto.getFilename() == null) return;
        Deque<AccountFileDto> lines = loadHistoryFileToDeque();

        // check whether this is the update of an existing line
        Optional<AccountFileDto> existingDto = lines.stream()
                .filter(lineDto -> Optional.ofNullable(lineDto.getGenerationTime())
                .orElse(Date.from(Instant.EPOCH)).equals(
                        Optional.ofNullable(dto.getGenerationTime()).orElse(new Date())))
                .findFirst();
        if(existingDto.isPresent()) {
            lines.remove(existingDto.get());
        }
        // we need to put the task at the first line, that is why we need Deque
        lines.offerFirst(dto);


        writeHistoryFileFromQueue(lines);
    }

    @Override
    public AccountFileDto getTask(String handleName) {
        return null;
    }

    /**
     * Loads whole file into Deque
     * @return
     * @throws IOException
     */
    private Deque<AccountFileDto> loadHistoryFileToDeque() throws IOException {
        InputStream is = storageService.loadAsInputStream(HISTORY_FILE);
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

    /**
     * Replaces the whole history file
     * @param dtos
     * @throws IOException
     */
    private void writeHistoryFileFromQueue(Queue<AccountFileDto> dtos) throws IOException {
        StringBuilder sb = new StringBuilder();
        for(AccountFileDto lineDto : dtos) {
            sb.append(objectMapper.writeValueAsString(lineDto)).append(System.lineSeparator());
        }
        storageService.store(sb.toString().getBytes(), HISTORY_FILE);
    }

}
