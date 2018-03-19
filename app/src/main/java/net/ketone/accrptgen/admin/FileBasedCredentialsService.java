package net.ketone.accrptgen.admin;

import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

@Service
public class FileBasedCredentialsService implements CredentialsService {

    public static String CREDENTIALS_FILE = "credentials.properties";

    @Autowired
    private StorageService storageService;


    @Override
    public Properties getCredentials() {
        Properties prop = new Properties();
        try {
            prop.load(storageService.load(CREDENTIALS_FILE));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }
}
