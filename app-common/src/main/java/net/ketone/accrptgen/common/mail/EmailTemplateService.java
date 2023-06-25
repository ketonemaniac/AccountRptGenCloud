package net.ketone.accrptgen.common.mail;

import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;

/**
 *  implementation is in tasks -- not needed in app
 */
public interface EmailTemplateService {

    String populateTemplate(final AccountJob job, final MailProperties properties);

}
