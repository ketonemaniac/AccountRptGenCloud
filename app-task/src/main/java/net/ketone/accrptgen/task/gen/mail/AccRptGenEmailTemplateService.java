package net.ketone.accrptgen.task.gen.mail;

import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.mail.EmailTemplateService;
import net.ketone.accrptgen.common.model.AccountJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Component
public class AccRptGenEmailTemplateService implements EmailTemplateService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public String populateTemplate(final AccountJob job, final MailProperties properties) {
        Context context = new Context();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("authentication", job.getSubmittedBy());
        model.put("job", job);
        context.setVariables(model);
        return templateEngine.process(properties.getTemplate(), context);
    }

}
