package net.ketone.accrptgen.service.credentials;

import net.ketone.accrptgen.service.store.StorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import static net.ketone.accrptgen.config.Constants.CREDENTIALS_FILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {FileBasedSettingsService.class})
public class FileBasedSettingsServiceTest {

    @MockBean(name = "persistentStorage")
    private StorageService persistentStorage;

    @Autowired
    private FileBasedSettingsService svc;

    private Properties prop = new Properties();

    @Before
    public void init() throws IOException {
        Mockito.when(persistentStorage.loadAsInputStream(CREDENTIALS_FILE)).thenAnswer(invocationOnMock -> {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            prop.store(os, "");
            ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
            return is;
        });
        Mockito.when(persistentStorage.store(any(), any())).thenAnswer(invocationOnMock -> {
            ByteArrayInputStream is = new ByteArrayInputStream(invocationOnMock.getArgument(0));
            prop.load(is);
            return "";
        });
    }


    @Test
    public void testLoadCredentials() {
        svc.saveSetting("hello", "world");
        Properties props = svc.getSettings();
        assertThat(props.getProperty("hello")).isEqualToIgnoringCase("world");
    }

}
