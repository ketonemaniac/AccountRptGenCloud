package net.ketone.accrptgen.common.mail;

import com.google.common.collect.ImmutableMap;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.domain.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractEmailService implements EmailService {

    @Autowired
    private UserService userService;
    @Autowired
    protected SettingsService configurationService;

    @Value("${mail.bcc:}")
    protected String EMAIL_BCC;


    protected Map<String, String[]> getEmailAddresses(AccountJob dto) {
        return ImmutableMap.of(
                "to", Optional.ofNullable(
                        userService.findByUsername(dto.getSubmittedBy()).block())  // code smell
                        .map(User::getEmail)
                        .map(s -> s.split(";"))
                        .orElse(new String[]{}),
                "cc", Optional.ofNullable(configurationService.getSettings())
                        .map(c -> c.getProperty(SettingsService.SEND_TO_PROP))
                        .map(s -> s.split(";"))
                        .orElse(new String[]{}),
                "bcc", Optional.ofNullable(EMAIL_BCC)
                        .map(s -> s.split(";"))
                        .orElse(new String[]{})
        );
    }
}
