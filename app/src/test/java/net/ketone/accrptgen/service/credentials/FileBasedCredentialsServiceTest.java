package net.ketone.accrptgen.service.credentials;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Builder;
import lombok.Data;
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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    @Test
    public void testLoadCredentialsMap() {
        svc.saveCredential("auditor.0.name", "auditor0");
        svc.saveCredential("auditor.0.banner", "banner0");
        svc.saveCredential("auditor.1.name", "auditor1");
        svc.saveCredential("auditor.1.banner", "banner1");
        Map<String, String> credentialsMap = svc.getCredentialsMap("auditor.");
        System.out.println("keyset=" + credentialsMap.keySet());
        assertThat(credentialsMap.get("auditor0")).isEqualTo("banner0");
        assertThat(credentialsMap.get("auditor1")).isEqualTo("banner1");

    }

}
