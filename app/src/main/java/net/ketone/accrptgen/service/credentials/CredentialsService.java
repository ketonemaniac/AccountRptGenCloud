package net.ketone.accrptgen.service.credentials;

import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

public interface CredentialsService {

    String SENDGRID_API_KEY_PROP = "mail.sendgrid.api-key";
    String SEND_TO_PROP = "mail.sendto";
    String PREPARSE_TEMPLATE_PROP = "xlsx.template.name";
    String MONGODB_PASS = "mongodb.password";

    Properties getCredentials();

    void saveCredential(String key, String value);

    void saveCredentials(Map<String, String> credentials);

}
