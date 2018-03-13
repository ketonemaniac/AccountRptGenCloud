package net.ketone.accrptgen.mail;

import java.io.InputStream;

public interface EmailService {

    void sendEmail(String companyName, String attachmentName, InputStream attachment) throws Exception;

}
