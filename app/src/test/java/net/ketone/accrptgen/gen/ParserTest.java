package net.ketone.accrptgen.gen;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@SpringBootTest
public class ParserTest {

    @Test
    public void testParseCell() throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource("/All documents 5.1.16.xlsm"));
        System.out.println(file.getAbsolutePath());
        new ParsingService().readFile(new FileInputStream(file));
    }
}
