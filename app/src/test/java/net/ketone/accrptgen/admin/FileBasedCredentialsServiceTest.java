package net.ketone.accrptgen.admin;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class FileBasedCredentialsServiceTest {

    @Autowired
    private FileBasedCredentialsService svc;

    @Test
    public void testLoadCredentials() {
        svc.saveCredential("hello", "world");
        Properties props = svc.getCredentials();
        assertThat(props.getProperty("hello")).isEqualToIgnoringCase("world");
    }

}
