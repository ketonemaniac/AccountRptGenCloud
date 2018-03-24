package net.ketone.accrptgen.admin;

import net.ketone.accrptgen.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
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
            InputStream is = storageService.load(CREDENTIALS_FILE);
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
            storageService.store(new ByteArrayInputStream(os.toByteArray()), CREDENTIALS_FILE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
