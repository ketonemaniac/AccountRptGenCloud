package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "excel-extract")
public class ExcelExtractProperties {

    private MergeProperties merge;

    private MailProperties mail;

}
