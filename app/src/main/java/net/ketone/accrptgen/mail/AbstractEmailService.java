package net.ketone.accrptgen.mail;

import net.ketone.accrptgen.admin.CredentialsService;
import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.auth.service.UserService;
import net.ketone.accrptgen.dto.AccountFileDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public abstract class AbstractEmailService implements EmailService {

    @Autowired
    private UserService userService;
    @Autowired
    protected CredentialsService credentialsService;

    @Value("${mail.bcc}")
    private String EMAIL_BCC;


    protected Map<String, String[]> getEmailAddresses(AccountFileDto dto) {
        return Map.of(
                "to", Optional.ofNullable(
                        userService.findByUsername(dto.getSubmittedBy()))
                        .map(User::getEmail)
                        .map(s -> s.split(";"))
                        .orElse(new String[]{}),
                "cc", Optional.ofNullable(credentialsService.getCredentials())
                        .map(c -> c.getProperty(CredentialsService.SEND_TO_PROP))
                        .map(s -> s.split(";"))
                        .orElse(new String[]{}),
                "bcc", Optional.ofNullable(EMAIL_BCC)
                        .map(s -> s.split(";"))
                        .orElse(new String[]{})
        );
    }
}
