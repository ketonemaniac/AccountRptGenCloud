package net.ketone.accrptgen.common.store;

import com.google.cloud.storage.*;
import com.google.common.base.Stopwatch;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
public class GCloudStorageService implements StorageService {

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
        log.info("storing " + filename);
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
        log.info("stored " + filename + " in " + stopwatch.toString());
        return filename;
    }

    @Override
    public InputStream loadAsInputStream(String filename) {
        return new ByteArrayInputStream(load(filename));
    }

    @Override
    public byte[] load(String filename) {
        log.info("loading " + filename);
        Stopwatch stopwatch = Stopwatch.createStarted();
        BlobId blobId = BlobId.of(bucketName, filename);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            log.warn("No such object");
            return new byte[0];
        }
        byte[] content = blob.getContent();
        log.info("loaded " + filename + " in " + stopwatch.toString());
        return content;
    }

    @Override
    public List<String> list() {
        return Optional.ofNullable(storage.get(bucketName))
                .map(Bucket::list)
                .map(blobs -> StreamSupport.stream(blobs.iterateAll().spliterator(), false)
                        .map(Blob::getName)
                        .filter(blob -> !blob.endsWith("/"))
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Override
    public void delete(String filename) {
        BlobId blobId = BlobId.of(bucketName, filename);
        if(blobId != null) {
            log.info("Deleting file " + filename);
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
