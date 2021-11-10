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

package net.ketone.accrptgen.common.mail;

import com.sendgrid.SendGrid;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.model.auth.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sendgrid service
 * @see https://cloud.google.com/compute/docs/tutorials/sending-mail/using-sendgrid
 */
@Slf4j
@Service
@ConditionalOnProperty(prefix="mail", name="enabled", havingValue = "true")
public class SendgridEmailService extends AbstractEmailService {

    private String SENDGRID_API_KEY;

    @Value("${mail.enabled}")
    private boolean SENDGRID_ENABLE;

    @Value("${mail.sender}")
    private String SENDGRID_SENDER;

    @Autowired
    protected SettingsService credentialsService;

    @Autowired
    private EmailTemplateService emailTemplateService;

    @PostConstruct
    public void init() {
        Properties props = credentialsService.getSettings();
        SENDGRID_API_KEY = props.getProperty(SettingsService.SENDGRID_API_KEY_PROP);
    }

    @Override
    public void sendEmail(AccountJob dto, List<Attachment> attachments,
                          final MailProperties properties) throws Exception {
        if(!SENDGRID_ENABLE) return;
        SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
        SendGrid.Email email = new SendGrid.Email();
        Map<String, String[]> recipients = getEmailAddresses(dto);
        email.addTo(recipients.get("to"));
        email.addCc(recipients.get("cc"));
        email.setFrom(SENDGRID_SENDER);
        email.setFromName("Accounting Report Generator");
        email.setBcc(recipients.get("bcc"));
        email.setSubject(properties.getSubjectPrefix() + dto.getCompany());
        email.setHtml(emailTemplateService.populateTemplate(dto, properties));

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
