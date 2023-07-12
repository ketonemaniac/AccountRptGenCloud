package net.ketone.accrptgen.task.gen.mail;

import net.ketone.accrptgen.common.config.properties.MailProperties;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.task.config.ThymeleafConfig;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
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
                .build(), MailProperties.builder().template("job-template.html").build());
        System.out.println(html);
        PrintWriter pw = new PrintWriter("target/out.html");
        pw.print(html);
        pw.close();
        Assertions.assertThat(html).isNotEmpty();
    }

}
