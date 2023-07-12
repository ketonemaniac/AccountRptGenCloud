package net.ketone.accrptgen.common.mail;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.model.auth.User;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@ConditionalOnProperty(prefix="mail", name="enabled", havingValue = "false")
public class MockEmailService extends AbstractEmailService {

    @Autowired
    private StorageService tempStorage;

    @Lazy
    @Autowired
    private EmailTemplateService emailTemplateService;

    @Override
    public void sendEmail(final AccountJob dto, final List<Attachment> attachments,
                          final MailProperties properties) throws Exception {
        tempStorage.store(emailTemplateService.populateTemplate(dto, properties)
                        .getBytes(StandardCharsets.UTF_8),
                FileUtils.uniqueFilename("out-", dto.getGenerationTime()) + ".html");
        Map<String, String[]> recipients = getEmailAddresses(dto);
        recipients.forEach((k,v) -> {
            log.info(k + ":" + Arrays.asList(v).stream().collect(Collectors.joining(";")));
        });
        for(Attachment attachment : attachments) {
            log.info("storing file " + attachment.getAttachmentName());
            tempStorage.store(attachment.getData(), attachment.getAttachmentName());
        }
    }

    @Override
    public void sendResetPasswordEmail(User user) throws Exception {
        log.info(String.format("User %s password reset: %s . Please change your password once logged in.",
                user.getUsername(), user.getPassword()));
    }
}
