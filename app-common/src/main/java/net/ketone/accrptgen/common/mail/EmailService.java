package net.ketone.accrptgen.common.mail;

import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.model.auth.User;

import java.util.List;

public interface EmailService {

    void sendEmail(AccountJob dto, List<Attachment> attachments) throws Exception;

    void sendResetPasswordEmail(User user) throws Exception;

}
