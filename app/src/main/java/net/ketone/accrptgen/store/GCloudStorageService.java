package net.ketone.accrptgen.store;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import com.google.common.base.Stopwatch;
import org.apache.commons.io.input.NullInputStream;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
@Profile({"gCloudStandard","gCloudFlexible"})
public class GCloudStorageService implements StorageService {

    // private static final Logger logger = LoggerFactory.getLogger(GCloudStorageService.class);
    private static final Logger logger = Logger.getLogger(GCloudStorageService.class.getName());

    Storage storage;
    static final String BUCKET_NAME = "accountrptgen-storage-test";

    @PostConstruct
    public void init() {
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();
        storage = optionsBuilder.build().getService();
    }


    @Override
    public String store(InputStream is, String filename) throws IOException {
        logger.info("storing " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        String contentType = null;
        BlobInfo.Builder blobInfoBuilder =
                BlobInfo.newBuilder(BUCKET_NAME, filename);
        if(filename.endsWith(".docx")) {
            blobInfoBuilder.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        } else if(filename.endsWith(".txt")) {
            blobInfoBuilder.setContentType("text/plain");
        } else if(filename.endsWith(".xlsx") || filename.endsWith(".xlsm")) {
            blobInfoBuilder.setContentType("application/vnd.ms-excel");
        }
        storage.create(blobInfoBuilder.build(), bytes);
        logger.info("stored " + filename + " in " + stopwatch.toString());
        is.close();
        return filename;
    }

    @Override
    public InputStream load(String filename) {
        logger.info("loading " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            System.out.println("No such object");
            return new NullInputStream(0);
        }
        byte[] content = blob.getContent();
        logger.info("loaded " + filename + " in " + stopwatch.toString());
        return new ByteArrayInputStream(content);
    }

    @Override
    public List<String> list() {

        List<String> filenames = new ArrayList<>();

        Bucket bucket = storage.get(BUCKET_NAME);
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
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        if(blobId != null) {
            logger.info("Deleting file " + filename);
            storage.delete(blobId);
        }
    }

}
