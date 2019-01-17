package net.ketone.accrptgen.store;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    /**
     * Write the doc
     * @param filename
     * @return the filename saved
     * @throws IOException
     */
    String store(byte[] input, String filename) throws IOException;

    InputStream loadAsInputStream(String filename);

    byte[] load(String filename);

    List<String> list();

    void delete(String filename);

}
