package net.ketone.accrptgen.admin;

import java.util.Map;
import java.util.Properties;

public interface CredentialsService {

    String SENDGRID_API_KEY_PROP = "mail.sendgrid.api-key";
    String SEND_TO_PROP = "mail.sendto";
    String PREPARSE_TEMPLATE_PROP = "xlsx.template.name";
    String MONGODB_PASS = "mongodb.password";

    Properties getCredentials();

    void saveCredential(String key, String value);

    void saveCredentials(Map<String, String> credentials);

}
