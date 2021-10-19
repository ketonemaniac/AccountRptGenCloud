package net.ketone.accrptgen.common.store;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"gCloudStandard"})
public class CloudStorageConfig {

    @Value("${storage.persistent.bucket}")
    private String persistentBucket;

    @Value("${storage.temp.bucket}")
    private String tempBucket;

    /**
     * For cloud profiles
     * Could be accessed directly (for non-cached objects) or via the 1st level cache
     * @return
     */
    @Bean
    public StorageService persistentStorage() {
        return new GCloudStorageService(persistentBucket);
    }

    @Bean
    public StorageService tempStorage() {
        return new GCloudStorageService(tempBucket);
    }

}
