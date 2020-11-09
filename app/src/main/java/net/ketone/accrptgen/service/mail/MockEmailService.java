package net.ketone.accrptgen.service.mail;

import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Profile("local")
public class MockEmailService extends AbstractEmailService {

    @Autowired
    private StorageService storageSvc;


    private static final Logger logger = Logger.getLogger(MockEmailService.class.getName());

    @Override
    public void sendEmail(AccountFileDto dto, List<Attachment> attachments) throws Exception {
        Map<String, String[]> recipients = getEmailAddresses(dto);
        recipients.forEach((k,v) -> {
            logger.info(k + ":" + Arrays.asList(v).stream().collect(Collectors.joining(";")));
        });
        for(Attachment attachment : attachments) {
            logger.info("storing file " + attachment.getAttachmentName());
            storageSvc.store(attachment.getData(), attachment.getAttachmentName());
        }
    }

    @Override
    public void sendResetPasswordEmail(User user) throws Exception {
        logger.info(String.format("User %s password reset: %s . Please change your password once logged in.",
                user.getUsername(), user.getPassword()));
    }
}
