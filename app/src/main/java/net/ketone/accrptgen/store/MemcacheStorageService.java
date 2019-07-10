package net.ketone.accrptgen.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.cache.Cache;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

/**
 * 1st level cache
 * Will NOT persist when store is called
 * But will try to find from peristent storage on retrieval
 */
public class MemcacheStorageService implements StorageService {

    @Autowired
    @Qualifier("persistentStorage")
    private StorageService persistentStorageService;


    private static final String FILE_PREFIX = "file-";
    private static final Logger logger = Logger.getLogger(MemcacheStorageService.class.getName());

    @Autowired
    private Cache cache;

    @Override
    public String store(byte[] input, String filename) throws IOException {
        logger.info("******** STORING " + FILE_PREFIX + filename);
        logger.fine("cache size before " + cache.size());
        cache.put(FILE_PREFIX + filename, input);
        logger.fine("cache size after " + cache.size());
        logger.fine("cache contains at store " + cache.containsKey(FILE_PREFIX + filename));
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        return new ByteArrayInputStream(load(filename));
    }

    @Override
    public byte[] load(String filename) {
        logger.info("******** FETCHING " + FILE_PREFIX + filename);
        logger.fine("cache size for fetch " + cache.size());
        logger.fine("cache contains at fetch " + cache.containsKey(FILE_PREFIX + filename));
        if(cache.containsKey(FILE_PREFIX + filename)) {
            byte[] data = (byte[]) cache.get(FILE_PREFIX + filename);
            return data;
        } else {
            // if cannot find in cache, try loading from storage
            return persistentStorageService.load(filename);
        }
    }

    @Override
    public List<String> list() {
        return null;
    }

    @Override
    public void delete(String filename) {
        cache.remove(FILE_PREFIX+filename);
    }
}
