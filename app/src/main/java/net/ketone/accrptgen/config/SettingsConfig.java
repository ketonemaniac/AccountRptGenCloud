package net.ketone.accrptgen.config;

import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.service.credentials.FileBasedSettingsService;
import net.ketone.accrptgen.service.store.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettingsConfig {

    @Bean
    public SettingsService credentialsService(final StorageService persistentStorage) {
        return new FileBasedSettingsService(persistentStorage, Constants.CREDENTIALS_FILE);
    }

    @Bean
    public SettingsService configurationService(final StorageService persistentStorage) {
        return new FileBasedSettingsService(persistentStorage, Constants.CONFIGURATION_FILE);
    }

}
