package net.ketone.accrptgen.app.service.gen.mail;

import net.ketone.accrptgen.app.config.ThymeleafConfig;
import net.ketone.accrptgen.common.model.AccountJob;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@WithMockUser(username = "admin")
@ContextConfiguration(classes = {AccRptGenEmailTemplateService.class, ThymeleafConfig.class})
public class AccRptGenEmailTemplateServiceITCase {

    @Autowired
    private AccRptGenEmailTemplateService service;

    @Test
    public void testTemplate() throws FileNotFoundException {
        String html = service.populateTemplate(AccountJob.builder()
                .company("ABC Company")
                .referredBy("Eric")
                .professionalFees(BigDecimal.valueOf(12000))
                .build());
        System.out.println(html);
        PrintWriter pw = new PrintWriter("out.html");
        pw.print(html);
        pw.close();
        assertThat(html).isNotEmpty();
    }

}
