package net.ketone.accrptgen.common.mail;

import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;

public interface EmailTemplateService {

    String populateTemplate(final AccountJob job, final MailProperties properties);

}
