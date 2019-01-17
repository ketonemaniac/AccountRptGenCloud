package net.ketone.accrptgen.store;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfiguration {

    @Profile({"gCloudStandard","gCloudFlexible"})
    @Bean(name="persistentStorage")
    public StorageService cloudPersistentStorage() {
        return new GCloudStorageService();
    }

    @Profile({"gCloudStandard","gCloudFlexible"})
    @Bean
    @Primary
    public StorageService cloudCacheStorage() {
        return new MemcacheStorageService();
    }


    @Profile("local")
    @Bean(name="persistentStorage")
    public StorageService localPersistentStorage() {
        return new FileStorageService();
    }

}
