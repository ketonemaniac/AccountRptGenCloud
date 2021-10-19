package net.ketone.accrptgen.common.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"local","test"})
public class LocalStorageConfig {

    @Value("${storage.persistent.folder}")
    private String persistentFolder;

    @Value("${storage.temp.folder}")
    private String tempFolder;

    @Bean
    public StorageService persistentStorage() {
        return new FileStorageService(persistentFolder);
    }

    @Bean
    public StorageService tempStorage() {
        return new FileStorageService(tempFolder);
    }

}
