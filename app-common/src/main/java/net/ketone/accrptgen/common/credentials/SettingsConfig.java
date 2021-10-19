package net.ketone.accrptgen.common.credentials;

import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.credentials.FileBasedSettingsService;
import net.ketone.accrptgen.common.store.EnableStorage;
import net.ketone.accrptgen.common.store.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableStorage
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
