package net.ketone.accrptgen.mail;

import java.io.InputStream;
import java.util.List;

public interface EmailService {

    void sendEmail(String companyName, List<Attachment> attachments) throws Exception;

}
