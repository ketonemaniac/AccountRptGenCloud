package net.ketone.accrptgen.mail;

import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.logging.Logger;

@Service
@Profile("local")
public class MockEmailService implements EmailService {

    @Autowired
    private StorageService storageSvc;


    private static final Logger logger = Logger.getLogger(MockEmailService.class.getName());

    @Override
    public void sendEmail(String companyName, List<Attachment> attachments) throws Exception {
        for(Attachment attachment : attachments) {
            logger.info("storing file " + attachment.getAttachmentName());
            ByteArrayInputStream is = new ByteArrayInputStream(attachment.getData());
            storageSvc.store(is, attachment.getAttachmentName());
            is.close();
        }
    }
}
