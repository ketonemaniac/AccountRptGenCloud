package net.ketone.accrptgen.admin;

import java.util.Map;
import java.util.Properties;

public interface CredentialsService {

    public static final String SENDGRID_API_KEY_PROP = "mail.sendgrid.api-key";
    public static final String SEND_TO_PROP = "mail.sendto";
    public static final String PREPARSE_TEMPLATE_PROP = "xlsx.template.name";


    Properties getCredentials();

    void saveCredential(String key, String value);

    void saveCredentials(Map<String, String> credentials);

}
