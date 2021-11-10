package net.ketone.accrptgen.task.util;

import io.vavr.Tuple;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellReference;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ExcelTaskUtilsTest {

    @Test
    public void testAddExcelImage() {

        try {
            ClassLoader classLoader = getClass().getClassLoader();


            Workbook workbook = ExcelTaskUtils.openExcelWorkbook(new FileInputStream(
                    classLoader.getResource("dBizFunding.xlsx").getFile()
            ));

            File file = new File(classLoader.getResource("TKH.png").getFile());

            InputStream inputStream = new FileInputStream(file);

            byte[] imageBytes = IOUtils.toByteArray(inputStream);
            inputStream.close();

            CellReference ref = new CellReference("A1");

            ExcelTaskUtils.insertImage(workbook, "Audit report", ref, imageBytes, Tuple.of(1.001,2.5));

            FileOutputStream fileOut = new FileOutputStream("target/testAddExcelImage.xlsx");
            workbook.write(fileOut);
            fileOut.close();
        }catch (Exception e) {
            System.out.println(e);
        }

    }

}
