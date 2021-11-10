package net.ketone.accrptgen.task.util;

import net.ketone.accrptgen.task.util.ZipUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ExtendWith(SpringExtension.class)
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
