package net.ketone.accrptgen.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import java.util.Collections;

@Configuration
public class MemcacheConfig {

    @Bean
    public Cache memcache() throws CacheException {
        CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
        return cacheFactory.createCache(Collections.emptyMap());
    }

}
