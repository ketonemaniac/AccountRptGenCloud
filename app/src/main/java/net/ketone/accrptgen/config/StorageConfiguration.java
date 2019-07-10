package net.ketone.accrptgen.config;

import net.ketone.accrptgen.store.FileStorageService;
import net.ketone.accrptgen.store.GCloudStorageService;
import net.ketone.accrptgen.store.MemcacheStorageService;
import net.ketone.accrptgen.store.StorageService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class StorageConfiguration {

    /**
     * For cloud profiles, 1st level is a cache.
     * The cache itself would have a reference to persistent storage
     * @return
     */
    @Profile({"gCloudStandard","gCloudFlexible"})
    @Bean
    @Primary
    public StorageService cloudCacheStorage() {
        return new MemcacheStorageService();
    }

    /**
     * For cloud profiles, 2nd level storage
     * Could be accessed directly (for non-cached objects) or via the 1st level cache
     * @return
     */
    @Profile({"gCloudStandard","gCloudFlexible"})
    @Bean(name="persistentStorage")
    public StorageService cloudPersistentStorage() {
        return new GCloudStorageService();
    }


    @Profile("local")
    @Bean(name="persistentStorage")
    public StorageService localPersistentStorage() {
        return new FileStorageService();
    }

}
