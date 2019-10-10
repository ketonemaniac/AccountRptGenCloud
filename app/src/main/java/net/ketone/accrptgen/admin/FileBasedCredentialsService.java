package net.ketone.accrptgen.admin;

import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static net.ketone.accrptgen.config.Constants.CREDENTIALS_FILE;

@Service
public class FileBasedCredentialsService implements CredentialsService {

    @Autowired
    @Qualifier("persistentStorage")
    private StorageService storageService;


    @Override
    public Properties getCredentials() {
        Properties prop = new Properties();
        try {
            InputStream is = storageService.loadAsInputStream(CREDENTIALS_FILE);
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
            prop.store(os, "File for sotring passwords and configurations");
            storageService.store(os.toByteArray(), CREDENTIALS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
