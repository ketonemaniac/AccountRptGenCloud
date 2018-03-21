package net.ketone.accrptgen.admin;

import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.store.StorageService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class FileBasedStatisticsServiceTest {

    @Autowired
    private FileBasedStatisticsService svc;
    @Autowired
    private StorageService storageService;

    @Test
    public void testSaveEmptyFile() throws IOException {
        storageService.delete(FileBasedStatisticsService.RECENTS_FILE);
        doSave();
    }

    @Test
    public void testSaveExistingFile() throws IOException {
        testSaveEmptyFile();
        doSave();
    }

    private AccountFileDto doSave() throws IOException {
        AccountFileDto dto = new AccountFileDto();
        dto.setGenerationTime(new Date());
        dto.setCompany("ABC Company Limited");
        dto.setStatus(AccountFileDto.Status.EMAIL_SENT.name());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        dto.setFilename("ABC-"+ sdf.format(dto.getGenerationTime()) + ".docx");
        svc.updateAccountReport(dto);
        return dto;
    }

    @Test
    public void testSaveExceedingMax() throws IOException {
        testSaveEmptyFile();
        for(int i = 0; i <= FileBasedStatisticsService.MAX_RECENTS; i++) {
            doSave();
        }
        List<AccountFileDto> dtos = svc.getRecentGenerations();
        assertEquals(FileBasedStatisticsService.MAX_RECENTS, dtos.size());
    }


    @Test
    public void testUpdate() throws IOException {
        storageService.delete(FileBasedStatisticsService.RECENTS_FILE);
        AccountFileDto dto = doSave();
        svc.updateAccountReport(dto);
        List<AccountFileDto> dtos = svc.getRecentGenerations();
        assertEquals(1, dtos.size());
    }

}
