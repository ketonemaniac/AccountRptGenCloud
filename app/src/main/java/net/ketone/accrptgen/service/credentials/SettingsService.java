package net.ketone.accrptgen.service.credentials;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public interface SettingsService {

    String SENDGRID_API_KEY_PROP = "mail.sendgrid.api-key";
    String SEND_TO_PROP = "mail.sendto";
    String PREPARSE_TEMPLATE_PROP = "xlsx.template.name";
    String PREPARSE_AUIDTPRG_TEMPLATE_PROP = "xlsx.template.auditprg.name";
    String MONGODB_PASS = "mongodb.password";

    Properties getSettings();

    void saveSetting(String key, String value);

    void saveSettings(Map<String, String> credentials);

}
