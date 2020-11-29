package net.ketone.accrptgen.service.credentials;

import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static net.ketone.accrptgen.config.Constants.CREDENTIALS_FILE;

@Service
public class FileBasedCredentialsService implements CredentialsService {

    @Autowired
    private StorageService persistentStorage;


    @Override
    public Properties getCredentials() {
        Properties prop = new Properties();
        try {
            InputStream is = persistentStorage.loadAsInputStream(CREDENTIALS_FILE);
            prop.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    @Override
    public void saveCredential(String key, String value) {
        saveCredentials(Collections.singletonMap(key, value));
    }

    @Override
    public void saveCredentials(Map<String, String> credentials) {
        Properties prop = getCredentials();
        credentials.entrySet().forEach(e -> {
            prop.setProperty(e.getKey(), e.getValue());
        });
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            prop.store(os, "File for storing passwords and configurations");
            persistentStorage.store(os.toByteArray(), CREDENTIALS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
