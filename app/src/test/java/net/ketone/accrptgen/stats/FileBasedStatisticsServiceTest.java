package net.ketone.accrptgen.stats;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.stats.FileBasedStatisticsService;
import net.ketone.accrptgen.store.StorageService;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;

@Ignore
@RunWith(SpringRunner.class)
@ActiveProfiles("local,test")
@SpringBootTest
public class FileBasedStatisticsServiceTest {

    @Autowired
    private FileBasedStatisticsService svc;
    @MockBean
    @Qualifier("persistentStorage")
    private StorageService storageService;

    private ObjectMapper objectMapper = new ObjectMapper();

    private void mockStorageLoad(List<AccountFileDto> dtos) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for(AccountFileDto dto : dtos) {
            byte[] dtoStr = objectMapper.writeValueAsString(dto).getBytes();
            os.write(dtoStr);
        }
        byte[] listByteArr = os.toByteArray();
        Mockito.when(storageService.loadAsInputStream(any())).thenReturn(new ByteArrayInputStream(listByteArr));
    }


    private AccountFileDto doSave() throws IOException {
        AccountFileDto dto = new AccountFileDto();
        dto.setGenerationTime(new Date());
        dto.setCompany("ABC Company Limited");
        dto.setStatus(AccountFileDto.Status.EMAIL_SENT.name());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        dto.setFilename("ABC-"+ sdf.format(dto.getGenerationTime()) + ".docx");
        svc.updateTask(dto);
        return dto;
    }

    @Test
    public void testSaveExceedingMax() throws IOException {
        mockStorageLoad(new ArrayList<>());
        for(int i = 0; i <= FileBasedStatisticsService.MAX_RECENTS; i++) {
            doSave();
        }
        List<AccountFileDto> dtos = svc.getRecentTasks();
        assertEquals(FileBasedStatisticsService.MAX_RECENTS, dtos.size());
    }


    @Test
    public void testUpdate() throws IOException {
        storageService.delete(FileBasedStatisticsService.HISTORY_FILE);
        AccountFileDto dto = doSave();
        svc.updateTask(dto);
        List<AccountFileDto> dtos = svc.getRecentTasks();
        assertEquals(1, dtos.size());
    }

    @Test
    public void testHousekeepTasks() throws Exception {
        Map<String, Integer> result = svc.housekeepTasks();
        for(String s : result.keySet()) {
            System.out.println(s + " " + result.get(s));
        }
        Assertions.assertThat(result.size()).isZero();
    }

}
