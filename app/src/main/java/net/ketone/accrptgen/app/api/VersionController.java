package net.ketone.accrptgen.app.api;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
class VersionController {

    @Value("${build.version}")
    private String buildVersion;

    @Value("${build.timestamp}")
    private String buildTimestamp;

    @GetMapping("/api/version")
    public Map<String, String> getVersion()  {
        var df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        var localDateTime = LocalDateTime.parse(buildTimestamp, df);
        var timestamp = df.format(localDateTime.toInstant(ZoneOffset.UTC).atOffset(ZoneOffset.of("+8")));
        return Map.of("version", buildVersion, "timestamp", timestamp);
    }

}