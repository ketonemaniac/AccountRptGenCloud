package net.ketone.accrptgen.service.store;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.common.base.Stopwatch;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GCloudStorageService implements StorageService {


    // private static final Logger logger = LoggerFactory.getLogger(GCloudStorageService.class);
    private static final Logger logger = Logger.getLogger(GCloudStorageService.class.getName());

    private Storage storage;

    private String bucketName;

    public GCloudStorageService(final String bucketName) {
        this.bucketName = bucketName;
    }

    @PostConstruct
    public void init() throws IOException {
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();
        storage = optionsBuilder.build().getService();
    }


    @Override
    public String store(byte[] input, String filename) throws IOException {
        logger.info("storing " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        String contentType = null;
        BlobInfo.Builder blobInfoBuilder =
                BlobInfo.newBuilder(bucketName, filename);
        if(filename.endsWith(".docx")) {
            blobInfoBuilder.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if(filename.endsWith(".txt")) {
            blobInfoBuilder.setContentType("text/plain");
        } else if(filename.endsWith(".xlsx") || filename.endsWith(".xlsm")) {
            blobInfoBuilder.setContentType("application/vnd.ms-excel");
        }
        storage.create(blobInfoBuilder.build(), input);
        logger.info("stored " + filename + " in " + stopwatch.toString());
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        return new ByteArrayInputStream(load(filename));
    }

    @Override
    public byte[] load(String filename) {
        logger.info("loading " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        BlobId blobId = BlobId.of(bucketName, filename);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            logger.warning("No such object");
            return new byte[0];
        }
        byte[] content = blob.getContent();
        logger.info("loaded " + filename + " in " + stopwatch.toString());
        return content;
    }

    @Override
    public List<String> list() {

        List<String> filenames = new ArrayList<>();

        Bucket bucket = storage.get(bucketName);
        if (bucket == null) {
            System.out.println("No such bucket");
            return new ArrayList<>();
        }
        Page<Blob> blobs = bucket.list();
        blobs.iterateAll().forEach(b -> filenames.add(b.getName()));
        return filenames;
    }

    @Override
    public void delete(String filename) {
        BlobId blobId = BlobId.of(bucketName, filename);
        if(blobId != null) {
            logger.info("Deleting file " + filename);
            storage.delete(blobId);
        }
    }

    @Override
    public boolean hasFile(String filename) {
        BlobId blobId = BlobId.of(bucketName, filename);
        Blob blob = storage.get(blobId);
        return (blob != null);
    }

}
