package net.ketone.accrptgen.app.config.properties;


import lombok.Data;
import net.ketone.accrptgen.app.domain.gen.Header;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@Data
@ConfigurationProperties(prefix = "parse")
public class HeaderProperties {

    private Map<String, HeaderSectionProperties> headers;

    @Data
    public static class HeaderSectionProperties {

        private Boolean companyName;

        private Header.Underline underline;

    }
}
