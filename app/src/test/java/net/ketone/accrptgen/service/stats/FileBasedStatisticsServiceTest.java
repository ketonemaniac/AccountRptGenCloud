package net.ketone.accrptgen.service.stats;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.store.StorageService;
import org.assertj.core.api.Assertions;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static net.ketone.accrptgen.config.Constants.HISTORY_FILE;
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

    private void mockStorageLoad(List<AccountJob> dtos) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for(AccountJob dto : dtos) {
            byte[] dtoStr = objectMapper.writeValueAsString(dto).getBytes();
            os.write(dtoStr);
        }
        byte[] listByteArr = os.toByteArray();
        Mockito.when(storageService.loadAsInputStream(any())).thenReturn(new ByteArrayInputStream(listByteArr));
    }


    private AccountJob doSave() throws IOException {
        AccountJob dto = new AccountJob();
        dto.setGenerationTime(LocalDateTime.now());
        dto.setCompany("ABC Company Limited");
        dto.setStatus(Constants.Status.EMAIL_SENT.name());
        dto.setSubmittedBy("user");
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
        List<AccountJob> dtos = svc.getRecentTasks("user");
        assertEquals(FileBasedStatisticsService.MAX_RECENTS, dtos.size());
    }


    @Test
    public void testUpdate() throws IOException {
        storageService.delete(HISTORY_FILE);
        AccountJob dto = doSave();
        svc.updateTask(dto);
        List<AccountJob> dtos = svc.getRecentTasks("user");
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
