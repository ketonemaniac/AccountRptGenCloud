package net.ketone.accrptgen.app.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
public class ZipUtilsTest {

    @Test
    public void testZipFiles() throws IOException {
        Map<String, byte[]> testInput = new HashMap<>();
        byte[] file1 = "yoyoyo".getBytes();
        byte[] file2 = "hihihi".getBytes();
        testInput.put("yo.txt", file1);
        testInput.put("hi.txt", file2);
        byte[] output = ZipUtils.zipFiles(testInput);
        File o= new File("target/testZipFiles.zip");
        FileOutputStream out = new FileOutputStream(o);
        out.write(output);
        out.close();
        // TODO: verify test results
    }


}
