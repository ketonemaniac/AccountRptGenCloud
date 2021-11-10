package net.ketone.accrptgen.common.credentials;

import java.util.Map;
import java.util.Properties;

public interface SettingsService {

    String SENDGRID_API_KEY_PROP = "mail.sendgrid.api-key";
    String SEND_TO_PROP = "mail.sendto";
    String PREPARSE_TEMPLATE_PROP = "xlsx.template.name";
    String PREPARSE_AUIDTPRG_TEMPLATE_PROP = "xlsx.template.auditprg.name";
    String PREPARSE_DBIZ_FUNDING_TEMPLATE_PROP = "xlsx.template.dBizFunding.name";
    String MONGODB_PASS = "mongodb.password";

    Properties getSettings();

    void saveSetting(String key, String value);

    void saveSettings(Map<String, String> credentials);

}
