/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ketone.accrptgen.service.mail;

import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.auth.User;
import net.ketone.accrptgen.domain.dto.AccountJob;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Sendgrid service
 * @see https://cloud.google.com/compute/docs/tutorials/sending-mail/using-sendgrid
 */
@Slf4j
@Service
@Profile("!local")
public class SendgridEmailService extends AbstractEmailService {

    private String SENDGRID_API_KEY;

    @Value("${mail.enabled}")
    private boolean SENDGRID_ENABLE;

    @Value("${mail.sender}")
    private String SENDGRID_SENDER;


    @PostConstruct
    public void init() {
        Properties props = credentialsService.getCredentials();
        SENDGRID_API_KEY = props.getProperty(CredentialsService.SENDGRID_API_KEY_PROP);
    }

    @Override
    public void sendEmail(AccountJob dto, List<Attachment> attachments) throws Exception {
        if(!SENDGRID_ENABLE) return;
        SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
        SendGrid.Email email = new SendGrid.Email();
        Map<String, String[]> recipients = getEmailAddresses(dto);
        email.addTo(recipients.get("to"));
        email.addCc(recipients.get("cc"));
        email.setFrom(SENDGRID_SENDER);
        email.setFromName("Accounting Report Generator");
        email.setBcc(recipients.get("bcc"));
        email.setSubject("Accounting Report For " + dto.getCompany());
        email.setText("Please find the accounting report for " + dto.getCompany() + " as attached. Referred by " + dto.getReferredBy());

        for(Attachment attachment : attachments) {
            InputStream data = new ByteArrayInputStream(attachment.getData());
            email.addAttachment(attachment.getAttachmentName(), data);
            data.close();
            log.info("Added attachment " + attachment.getAttachmentName());
        }

        SendGrid.Response response = sendgrid.send(email);
        if (response.getCode() != 200) {
            log.warn(String.format("An error occured: %s Code=%d", response.getMessage(), response.getCode()));
            return;
        }
        recipients.forEach((k,v) -> {
            log.info("email " + k + ":" + Arrays.asList(v).stream().collect(Collectors.joining(";")));
        });

    }

    @Override
    public void sendResetPasswordEmail(User user) throws Exception {
        if(!SENDGRID_ENABLE) return;
        SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
        SendGrid.Email email = new SendGrid.Email();
        email.addTo(user.getEmail());
        email.setFrom(SENDGRID_SENDER);
        email.setFromName("Accounting Report Generator");
        email.setBcc(Optional.ofNullable(EMAIL_BCC)
                .map(s -> s.split(";"))
                .orElse(new String[]{}));
        email.setSubject("Accounting Report Generator Password Reset");
        email.setText(String.format("User %s password reset: %s . Please change your password once logged in.",
                user.getUsername(), user.getPassword()));

        SendGrid.Response response = sendgrid.send(email);
        if (response.getCode() != 200) {
            log.warn(String.format("An error occured: %s Code=%d", response.getMessage(), response.getCode()));
            return;
        }
    }

}
