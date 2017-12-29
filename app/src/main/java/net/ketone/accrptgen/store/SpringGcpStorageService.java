package net.ketone.accrptgen.store;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring GCP way of storage
 */
@Component
@Profile("spring-gcp")
public class SpringGcpStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(SpringGcpStorageService.class);
    List<String> filenames = new ArrayList<>();

    @Value("gs://accountrptgen-storage-test/")
    private Resource bucketResource;

    @Override
    public String store(XWPFDocument doc, String filename) throws IOException {
        Resource relative = this.bucketResource.createRelative(filename);
        try (OutputStream os = ((WritableResource) relative).getOutputStream()) {
            doc.write(os);
            os.close();
            // os.write(data.getBytes());
        } finally {
            doc.close();
        }


        return filename;
    }

    /*@Override
    public String store(MultipartFile file) throws IOException {
        String filename =
                generateFileNameWithTime(file.getOriginalFilename());
        Resource relative = this.bucketResource.createRelative(filename);

        try (OutputStream os = ((WritableResource) relative).getOutputStream()) {
            os.write(file.getBytes());
            os.close();
            filenames.add(filename);
            logger.info("File stored: " + filename);
        } catch (IOException e) {
            logger.info("Error saving File: " + filename);
            throw e;
        }
        return filename;
    }*/

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Resource res = this.bucketResource.createRelative(filename);
            return new InputStreamResource(res.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> list() {
        return null;
    }

    @Override
    public void delete(String filename) {

    }

}
