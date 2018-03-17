package net.ketone.accrptgen.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

@Service
public class FileBasedStatisticsService implements StatisticsService {

    private ObjectMapper objectMapper = new ObjectMapper();

    public static String STATS_FILE = "statistics.txt";
    public static String RECENTS_FILE = "recents.txt";
    public static int MAX_RECENTS = 10;

    @Autowired
    private StorageService storageService;


    @Override
    public Map<String, Integer> getGenerationCounts() {
        return null;
    }

    @Override
    public List<AccountFileDto> getRecentGenerations() {
        InputStream is = storageService.load(RECENTS_FILE);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        List<AccountFileDto> recently = buf.lines().map(s -> {
            try {
                return objectMapper.readValue(s, AccountFileDto.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new AccountFileDto();
        }).collect(Collectors.toList());
        try {
            is.close();
        } catch (IOException e) {}
        return Lists.reverse(recently); // put latest first
    }

    @Override
    public void updateAccountReport(AccountFileDto dto) throws IOException {
        InputStream is = storageService.load(RECENTS_FILE);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));
        Queue<AccountFileDto> lines = buf.lines()
                .map(line -> {
                    try {
                        return objectMapper.readValue(line, AccountFileDto.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return new AccountFileDto();
                })
                .collect(Collectors.toCollection(LinkedBlockingQueue::new));
        is.close();
        boolean isUpdate = false;
        for(AccountFileDto lineDto : lines) {
            if(lineDto.getFilename().equals(dto.getFilename())) {
                lineDto.setStatus(dto.getStatus());
                isUpdate = true;
            }
        }
        if(!isUpdate) {
            lines.add(dto);
        }
        while(lines.size() > MAX_RECENTS) { // fit to max - 1
            lines.remove();
        }
        StringBuilder sb = new StringBuilder();
        for(AccountFileDto lineDto : lines) {
            sb.append(objectMapper.writeValueAsString(lineDto)).append(System.lineSeparator());
        }
        InputStream output = new ByteArrayInputStream(sb.toString().getBytes());
        storageService.store(output, RECENTS_FILE);
        output.close();
    }
}
