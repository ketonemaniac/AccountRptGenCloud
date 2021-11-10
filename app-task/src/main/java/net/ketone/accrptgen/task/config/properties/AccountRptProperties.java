package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "accountrpt")
public class AccountRptProperties {

    private ParseProperties parse;

    private MergeProperties merge;

    private MailProperties mail;

}
