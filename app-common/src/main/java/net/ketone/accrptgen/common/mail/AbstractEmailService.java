package net.ketone.accrptgen.common.mail;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableMap;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.domain.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractEmailService implements EmailService {

    @Autowired
    private UserService userService;
    @Autowired
    protected SettingsService configurationService;

    @Value("${mail.bcc:}")
    protected String EMAIL_BCC;


    protected Map<String, String[]> getEmailAddresses(AccountJob dto) {
        return userService.findByUsername(dto.getSubmittedBy())
                .map(user -> ImmutableMap.of(
                        "to", Optional.ofNullable(user)  // code smell
                                .map(User::getEmail)
                                .map(s -> s.split(";"))
                                .orElse(new String[]{}),
                        "cc", Stream.concat(Optional.ofNullable(configurationService.getSettings())
                                .map(c -> c.getProperty(SettingsService.SEND_TO_PROP))
                                .map(s -> s.split(";"))
                                .map(Arrays::asList)
                                .orElse(Lists.newArrayList())
                                .stream(),
                                Optional.ofNullable(user.getCc())
                                .orElse(Lists.newArrayList())
                                .stream()).collect(Collectors.toList())
                                .toArray(new String[0]),
                        "bcc", Optional.ofNullable(EMAIL_BCC)
                                .map(s -> s.split(";"))
                                .orElse(new String[]{})))
                .block();
    }
}
