package net.ketone.accrptgen.service.mail;

import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class EmailTemplateService {

    @Autowired
    private SpringTemplateEngine templateEngine;

    public String populateTemplate(final AccountJob job) {
        Context context = new Context();
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("authentication", UserUtils.getAuthenticatedUser());
        model.put("job", job);
        context.setVariables(model);
        return templateEngine.process("job-template", context);
    }

}
