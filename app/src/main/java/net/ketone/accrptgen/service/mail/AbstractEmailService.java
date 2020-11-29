package net.ketone.accrptgen.service.mail;

import com.google.common.collect.ImmutableMap;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.service.auth.UserService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractEmailService implements EmailService {

    @Autowired
    private UserService userService;
    @Autowired
    protected CredentialsService credentialsService;

    @Value("${mail.bcc}")
    protected String EMAIL_BCC;


    protected Map<String, String[]> getEmailAddresses(AccountJob dto) {
        return ImmutableMap.of(
                "to", Optional.ofNullable(
                        userService.findByUsername(dto.getSubmittedBy()).block())  // code smell
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
