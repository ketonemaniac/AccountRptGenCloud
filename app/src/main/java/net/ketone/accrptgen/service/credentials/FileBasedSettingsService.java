package net.ketone.accrptgen.service.credentials;

import lombok.RequiredArgsConstructor;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@RequiredArgsConstructor
public class FileBasedSettingsService implements SettingsService {

    private final StorageService persistentStorage;

    private final String fileName;

    @Override
    public Properties getSettings() {
        Properties prop = new Properties();
        try {
            InputStream is = persistentStorage.loadAsInputStream(fileName);
            prop.load(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return prop;
    }

    @Override
    public void saveSetting(String key, String value) {
        saveSettings(Collections.singletonMap(key, value));
    }

    @Override
    public void saveSettings(Map<String, String> credentials) {
        Properties prop = getSettings();
        credentials.entrySet().forEach(e -> {
            prop.setProperty(e.getKey(), e.getValue());
        });
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            prop.store(os, "File for storing passwords and configurations");
            persistentStorage.store(os.toByteArray(), fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
