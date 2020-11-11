package net.ketone.accrptgen.service.store;

import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.domain.dto.AccountJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class StorageServiceTest {

    @Autowired
    private StorageService storageService;

    private static final String HISTORY_FILE = "target" + File.separator + Constants.HISTORY_FILE;

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

    private AccountJob doSave() throws IOException {
        AccountJob dto = new AccountJob();
        dto.setGenerationTime(new Date());
        dto.setCompany("ABC Company Limited");
        dto.setStatus(Constants.Status.EMAIL_SENT.name());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        dto.setFilename("ABC-"+ sdf.format(dto.getGenerationTime()) + ".docx");
        return dto;
    }
}
