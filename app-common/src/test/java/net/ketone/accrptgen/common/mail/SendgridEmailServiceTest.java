package net.ketone.accrptgen.common.mail;

import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.domain.user.UserService;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {SendgridEmailService.class, SendgridEmailServiceTest.TestContext.class})
@TestPropertySource(properties = "mail.enabled=true")
public class SendgridEmailServiceTest {

    @MockitoBean
    private UserService userService;

    @Configuration
    static class TestContext {

        @Bean
        @Primary
        public SettingsService credentialsService() {
            var credentialsService = Mockito.mock(SettingsService.class);
            Mockito.when(credentialsService.getSettings()).thenReturn(new Properties());
            return credentialsService;
        }
    }

    @MockitoBean
    private EmailTemplateService emailTemplateService;

    @Autowired
    private SendgridEmailService sendgridEmailService;


    @Test
    public void testPopulateRecipients() throws Exception {
        Map<String, String[]> recipients = Map.of("to", Arrays.array("a@a.com", "b@b.com"),
                "cc", Arrays.array("b@b.com", "c@c.com"),
                "bcc", Arrays.array("c@c.com", "d@d.com"));
        Personalization personalization = sendgridEmailService.populateRecipients(recipients, false);
        Assertions.assertThat(personalization.getTos().stream().map(Email::getEmail).collect(Collectors.toUnmodifiableList()))
        .hasSameElementsAs(List.of("a@a.com", "b@b.com"));
        Assertions.assertThat(personalization.getCcs().stream().map(Email::getEmail).collect(Collectors.toUnmodifiableList()))
                .hasSameElementsAs(List.of("c@c.com"));
        Assertions.assertThat(personalization.getBccs().stream().map(Email::getEmail).collect(Collectors.toUnmodifiableList()))
                .hasSameElementsAs(List.of("d@d.com"));

    }

}