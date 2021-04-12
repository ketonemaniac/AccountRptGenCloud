package net.ketone.accrptgen.util;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ExcelUtils {

    public static XSSFWorkbook openExcelWorkbook(final byte[] workbookArr) throws IOException {
        return new XSSFWorkbook(new ByteArrayInputStream(workbookArr));
    }

    public static XSSFWorkbook openExcelWorkbook(final InputStream workbookStream) throws IOException {
        try(workbookStream) {
            return new XSSFWorkbook(workbookStream);
        } finally {
            workbookStream.close();
        }
    }

}
