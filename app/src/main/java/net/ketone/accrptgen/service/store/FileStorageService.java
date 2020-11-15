package net.ketone.accrptgen.service.store;

import org.apache.commons.io.input.NullInputStream;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class FileStorageService implements StorageService {

//    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final Logger logger = Logger.getLogger(FileStorageService.class.getName());

    private String storageFolder;

    public FileStorageService(final String storageFolder) {
        this.storageFolder = storageFolder;
    }

    @Override
    public String store(byte[] input, String filename) throws IOException {
        logger.info("Writing to local file " + storageFolder + filename);
        File output= new File(storageFolder + filename);
        FileOutputStream out = new FileOutputStream(output);
        out.write(input);
        out.close();
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        try {
            return new FileInputStream(storageFolder + filename);
        } catch (FileNotFoundException e) {
            return new NullInputStream(0);
        }
    }

    @Override
    public byte[] load(String filename) {
        try {
            return Files.readAllBytes(new File(storageFolder + filename).toPath());
        } catch (IOException e) {
            return new byte[0];
        }
    }

    @Override
    public List<String> list() {
        File f = new File(storageFolder);
        return Arrays.asList(f.list());
    }

    @Override
    public void delete(String filename) {
        File f = new File(storageFolder + filename);
        if(f.exists()) {
            logger.info("Deleting local file " + storageFolder + filename);
            boolean success = f.delete();
            logger.info("Deleted=" + success);
        }
    }

    @Override
    public boolean hasFile(String filename) {
        File f = new File(storageFolder + filename);
        return f.exists();
    }

}
