package net.ketone.accrptgen.service.store;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface StorageService {

    String ALLDOCS_PATH = "allDocs" + File.separator;
    String AUDIT_PRG_PATH = "auditPrg" + File.separator;
    String BANNER_PATH = "banner" + File.separator;

    /**
     * Write the doc
     * @param filename
     * @return the filename saved
     * @throws IOException
     */
    String store(byte[] input, String filename) throws IOException;

    InputStream loadAsInputStream(String filename);

    byte[] load(String filename);

    /**
     * List all files including files in sub-directories
     * @return
     */
    List<String> list();

    void delete(String filename);

    boolean hasFile(String filename);
}
