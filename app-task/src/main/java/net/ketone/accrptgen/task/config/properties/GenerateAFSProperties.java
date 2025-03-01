package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import net.ketone.accrptgen.common.config.properties.MailProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "generate-afs")
public class GenerateAFSProperties {

    private MailProperties mail;

    private List<String> auditSheets;

}
