package net.ketone.accrptgen.service.gen;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface FileProcessor<T> {

    T process(final byte[] input) throws IOException;

}
