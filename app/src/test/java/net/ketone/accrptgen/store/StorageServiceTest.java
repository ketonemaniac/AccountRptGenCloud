package net.ketone.accrptgen.store;

import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.stats.FileBasedStatisticsService;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class StorageServiceTest {

    @Autowired
    private StorageService storageService;

    private static final String HISTORY_FILE = "target" + File.separator + FileBasedStatisticsService.HISTORY_FILE;

    @Test
    public void testSaveEmptyFile() throws IOException {
        storageService.delete(HISTORY_FILE);
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
        return dto;
    }
}
