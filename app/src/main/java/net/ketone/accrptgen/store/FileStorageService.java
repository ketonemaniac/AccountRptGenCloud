package net.ketone.accrptgen.store;

import org.apache.commons.io.input.NullInputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class FileStorageService implements StorageService {

//    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());

    @Value("${storage.folder}")
    private String STORAGE_FOLDER;

    List<String> filenames = new ArrayList<>();

    @Override
    public String store(byte[] input, String filename) throws IOException {
        logger.info("Writing to local file " + STORAGE_FOLDER + filename);
        File output= new File(STORAGE_FOLDER + filename);
        FileOutputStream out = new FileOutputStream(output);
        out.write(input);
        out.close();
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        try {
            return new FileInputStream(STORAGE_FOLDER + filename);
        } catch (FileNotFoundException e) {
            return new NullInputStream(0);
        }
    }

    @Override
    public byte[] load(String filename) {
        try {
            return Files.readAllBytes(new File(STORAGE_FOLDER + filename).toPath());
        } catch (IOException e) {
            return new byte[0];
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

    @Override
    public boolean hasFile(String filename) {
        File f = new File(STORAGE_FOLDER + filename);
        return f.exists();
    }

}
