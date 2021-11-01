package net.ketone.accrptgen.task.config.properties;

import lombok.Data;
import net.ketone.accrptgen.task.gen.model.Header;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
public class ParseProperties {

    private Map<String, HeaderSectionProperties> headers;

    @Data
    public static class HeaderSectionProperties {

        private Boolean companyName;

        private Header.Underline underline;

    }

    private String keepFormulaColor;

}
