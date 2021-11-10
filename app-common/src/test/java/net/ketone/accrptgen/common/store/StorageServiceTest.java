package net.ketone.accrptgen.common.store;

import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.model.AccountJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@SpringBootTest(classes={LocalStorageConfig.class})
@TestPropertySource(properties = {
        "storage.persistent.folder=target/test",
        "storage.temp.folder=target/test"
})
public class StorageServiceTest {

    @Autowired
    private StorageService persistentStorage;

    private static final String HISTORY_FILE = "target" + File.separator + Constants.HISTORY_FILE;

    @Test
    public void testSaveEmptyFile() throws IOException {
        persistentStorage.delete(HISTORY_FILE);
        doSave();
    }

    @Test
    public void testSaveExistingFile() throws IOException {
        testSaveEmptyFile();
        doSave();
    }

    private AccountJob doSave() throws IOException {
        AccountJob dto = AccountJob.builder()
                .generationTime(LocalDateTime.now())
                .company("ABC Company Limited")
                .status(Constants.Status.EMAIL_SENT.name())
                .build();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
        dto.setFilename("ABC-"+ df.format(dto.getGenerationTime()) + ".docx");
        return dto;
    }
}
