package net.ketone.accrptgen.admin;

import net.ketone.accrptgen.store.StorageService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
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
@ContextConfiguration(classes = {FileBasedCredentialsService.class})
public class FileBasedCredentialsServiceTest {

    @MockBean(name = "persistentStorage")
    private StorageService persistentStorage;

    @Autowired
    private FileBasedCredentialsService svc;

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
        svc.saveCredential("hello", "world");
        Properties props = svc.getCredentials();
        assertThat(props.getProperty("hello")).isEqualToIgnoringCase("world");
    }

}
