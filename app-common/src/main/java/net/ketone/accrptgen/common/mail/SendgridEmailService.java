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

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Attachments;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Personalization;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.model.auth.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
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

    @Lazy
    @Autowired
    private EmailTemplateService emailTemplateService;

    @PostConstruct
    public void init() {
        Properties props = credentialsService.getSettings();
        SENDGRID_API_KEY = props.getProperty(SettingsService.SENDGRID_API_KEY_PROP);
    }

    protected Personalization populateRecipients(Map<String, String[]> recipients, boolean noCCemail) {
        Set<String> addedEmails = new HashSet<>();
        Personalization personalization = new Personalization();
        Arrays.stream(recipients.get("to"))
                .filter(StringUtils::isNotEmpty)
                .filter(to -> !addedEmails.contains(to))
                .forEach(to -> {
                    addedEmails.add(to);
                    personalization.addTo(new Email(to, to));
                });
        if(!noCCemail) {
            Arrays.stream(recipients.get("cc"))
                    .filter(StringUtils::isNotEmpty)
                    .filter(cc -> !addedEmails.contains(cc))
                    .forEach(cc -> {
                        addedEmails.add(cc);
                        personalization.addCc(new Email(cc, cc));
                    });
            Arrays.stream(recipients.get("bcc"))
                    .filter(StringUtils::isNotEmpty)
                    .filter(bcc -> !addedEmails.contains(bcc))
                    .forEach(bcc -> {
                        addedEmails.add(bcc);
                        personalization.addBcc(new Email(bcc, bcc));
                    });
        }
        return personalization;
    }

    @Override
    public void sendEmail(AccountJob dto, List<Attachment> attachments,
                          final MailProperties properties) throws Exception {
        if(!SENDGRID_ENABLE) return;
        Mail email = new Mail();
        Map<String, String[]> recipients = getEmailAddresses(dto);
        email.addPersonalization(populateRecipients(recipients, dto.getNoCCemail()));
        email.setFrom(new Email(SENDGRID_SENDER, "Accounting Report Generator"));
        email.setSubject(String.format("%s%s %s", Optional.ofNullable(dto.getFundingType()).orElse(
                StringUtils.EMPTY), properties.getSubjectPrefix(), dto.getCompany()));
        email.addContent(new Content("text/html", emailTemplateService.populateTemplate(dto, properties)));

        for(Attachment attachment : attachments) {
            InputStream data = new ByteArrayInputStream(attachment.getData());
            Attachments emailAttachment = new Attachments();
            emailAttachment.setContent(Base64.getEncoder().encodeToString(data.readAllBytes()));
            emailAttachment.setFilename(attachment.getAttachmentName());
            emailAttachment.setType("application/zip");
            emailAttachment.setDisposition("attachment");

            email.addAttachments(emailAttachment);
            data.close();
            log.info("Added attachment " + attachment.getAttachmentName());
        }

        sendMail(email);
        recipients.forEach((k,v) -> {
            log.info("email " + k + ":" + Arrays.asList(v).stream().collect(Collectors.joining(";")));
        });

    }

    @Override
    public void sendResetPasswordEmail(User user) throws Exception {
        if(!SENDGRID_ENABLE) return;
        Mail email = new Mail();
        Personalization personalization = new Personalization();
        personalization.addTo(new Email(user.getEmail(), user.getEmail()));
        email.addPersonalization(personalization);
        email.setFrom(new Email(SENDGRID_SENDER, "Accounting Report Generator"));
        email.setSubject("Accounting Report Generator Password Reset");

        email.addContent(new Content("text/html",
                String.format("User %s password reset: %s . Please change your password once logged in.",
                        user.getUsername(), user.getPassword())));
        sendMail(email);
    }

    private void sendMail(Mail email) throws Exception {
        SendGrid sendgrid = new SendGrid(SENDGRID_API_KEY);
        final Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(email.build());

        Response response = sendgrid.api(request);
        if (!HttpStatus.valueOf(response.getStatusCode()).is2xxSuccessful()) {
            log.warn(String.format("An error occurred: Code=%d %s %s", response.getStatusCode(), response.getHeaders().toString(),
                    response.getBody()));
        }
    }

}
