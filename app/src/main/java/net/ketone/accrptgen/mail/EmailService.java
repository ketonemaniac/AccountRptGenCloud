package net.ketone.accrptgen.mail;

import net.ketone.accrptgen.auth.model.User;
import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.InputStream;
import java.util.List;

public interface EmailService {

    void sendEmail(AccountFileDto dto, List<Attachment> attachments) throws Exception;

    void sendResetPasswordEmail(User user) throws Exception;

}
