package net.ketone.accrptgen.util;

import java.io.*;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static byte[] zipFiles(Map<String, byte[]> fileMap) throws IOException {
        ByteArrayOutputStream fos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(fos);
        for (String fileName : fileMap.keySet()) {
            ByteArrayInputStream fis = new ByteArrayInputStream(fileMap.get(fileName));
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
            fis.close();
        }
        zipOut.close();
        return fos.toByteArray();
    }

}
