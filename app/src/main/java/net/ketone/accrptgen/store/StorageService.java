package net.ketone.accrptgen.store;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

    boolean hasFile(String filename);
}
