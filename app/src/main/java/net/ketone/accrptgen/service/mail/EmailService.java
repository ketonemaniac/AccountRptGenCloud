package net.ketone.accrptgen.service.mail;

import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.dto.AccountFileDto;

import java.util.List;

public interface EmailService {

    void sendEmail(AccountFileDto dto, List<Attachment> attachments) throws Exception;

    void sendResetPasswordEmail(User user) throws Exception;

}
