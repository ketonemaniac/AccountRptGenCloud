package net.ketone.accrptgen.store;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("gcloud")
public class GCloudStorageService implements StorageService {

    Storage storage;
    static final String BUCKET_NAME = "accountrptgen-storage-test";

    @PostConstruct
    public void init() {
        StorageOptions.Builder optionsBuilder = StorageOptions.newBuilder();
        storage = optionsBuilder.build().getService();
    }


    @Override
    public String store(InputStream is, String filename) throws IOException {

        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        String contentType = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        BlobInfo blobInfo =
                BlobInfo.newBuilder(BUCKET_NAME, filename).setContentType(contentType).build();
        storage.create(blobInfo, bytes);
        return filename;
    }

    @Override
    public Resource loadAsResource(String filename) {
        BlobId blobId = BlobId.of(BUCKET_NAME, filename);
        Blob blob = storage.get(blobId);
        if (blob == null) {
            System.out.println("No such object");
            return null;
        }
        byte[] content = blob.getContent();
        return new InputStreamResource(new ByteArrayInputStream(content));
    }

    @Override
    public List<String> list() {

        List<String> filenames = new ArrayList<>();

        Bucket bucket = storage.get(BUCKET_NAME);
        if (bucket == null) {
            System.out.println("No such bucket");
            return null;
        }
        Page<Blob> blobs = bucket.list();
        blobs.iterateAll().forEach(b -> filenames.add(b.getName()));
        return filenames;
    }

    @Override
    public void delete(String filename) {

    }

    @Override
    public XSSFWorkbook getTemplate(String templateName) throws IOException {
        BlobId blobId = BlobId.of(BUCKET_NAME, templateName);
        Blob blob = storage.get(blobId);
        byte[] content = blob.getContent();
        return new XSSFWorkbook(new ByteArrayInputStream(content));
    }
}
