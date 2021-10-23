package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "accountrpt")
public class AccountRptProperties {

    private ParseProperties parse;

    private MergeProperties merge;

}
