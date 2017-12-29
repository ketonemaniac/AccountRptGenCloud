package net.ketone.accrptgen.store;

import net.ketone.accrptgen.controller.AccRptGenController;
import net.ketone.accrptgen.gen.ParsingService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

@Component
@Profile("local")
public class FileStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String STORAGE_FOLDER = "files" + File.separator + "in" + File.separator;
    List<String> filenames = new ArrayList<>();

    @Override
    public String store(XWPFDocument doc, String filename) throws IOException {
        File output= new File(STORAGE_FOLDER + filename);

        FileOutputStream out = new FileOutputStream(output);
        doc.write(out);
        out.close();
        doc.close();
        return filename;
    }

    /*
    public String store(MultipartFile file) throws IOException {
        File convFile = null;
        String pathName = STORAGE_FOLDER +
                generateFileNameWithTime(file.getOriginalFilename());
        try {
            convFile = new File(pathName);
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();
            filenames.add(convFile.getCanonicalPath());
            logger.info("File stored: " + convFile.getName());
        } catch (IOException e) {
            logger.info("Error saving File: " + convFile.getCanonicalFile());
            throw e;
        }
        return convFile.getName();
    }

    private String generateFileNameWithTime(String fileName) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");
        int extLoc = fileName.lastIndexOf(".");
        if(extLoc == -1) {
            return fileName + "-" + sdf.format(new Date());
        }
        String name = fileName.substring(0, extLoc);
        String extenstion = fileName.substring(extLoc, fileName.length());
        return name + "-" + sdf.format(new Date()) + extenstion;
    }
    */

    @Override
    public Resource loadAsResource(String filename) {
        File f= new File(STORAGE_FOLDER + filename);
        FileSystemResource resource = new FileSystemResource(f);
        return resource;
    }

    @Override
    public List<String> list() {
        File f = new File(STORAGE_FOLDER);
        return Arrays.asList(f.list());
    }

    @Override
    public void delete(String filename) {

    }

}
