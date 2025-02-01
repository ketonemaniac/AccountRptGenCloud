package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "breakdown-tabs")
public class BreakdownTabsProperties {

    private MergeProperties merge;

    private MailProperties mail;

    private List<String> fixSheets;

    private List<String> banSheets;

    private List<String> auditSheets;

    private Map<String, String> scheduleSheetColumn;

}
