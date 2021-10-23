package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "excel-extract")
public class ExcelExtractProperties {

    private MergeProperties merge;

}
