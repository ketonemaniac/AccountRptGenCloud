package net.ketone.accrptgen.service.mail;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("local")
public class MockEmailService extends AbstractEmailService {

    @Autowired
    private StorageService tempStorage;

    @Override
    public void sendEmail(AccountJob dto, List<Attachment> attachments) throws Exception {
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
