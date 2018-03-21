package net.ketone.accrptgen.store;

import org.apache.commons.io.input.NullInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Profile("local")
public class FileStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String STORAGE_FOLDER = "files" + File.separator;
    List<String> filenames = new ArrayList<>();

    @Override
    public String store(InputStream is, String filename) throws IOException {
        logger.info("Writing to local file " + STORAGE_FOLDER + filename);
        File output= new File(STORAGE_FOLDER + filename);
        FileOutputStream out = new FileOutputStream(output);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        out.write(bytes);
        out.close();
        return filename;
    }

    @Override
    public InputStream load(String filename) {
        try {
            return new FileInputStream(STORAGE_FOLDER + filename);
        } catch (FileNotFoundException e) {
            return new NullInputStream(0);
        }
    }

    @Override
    public List<String> list() {
        File f = new File(STORAGE_FOLDER);
        return Arrays.asList(f.list());
    }

    @Override
    public void delete(String filename) {
        File f = new File(STORAGE_FOLDER + filename);
        if(f.exists()) {
            logger.info("Deleting local file " + STORAGE_FOLDER + filename);
            boolean success = f.delete();
            logger.info("Deleted=" + success);
        }
    }

}
