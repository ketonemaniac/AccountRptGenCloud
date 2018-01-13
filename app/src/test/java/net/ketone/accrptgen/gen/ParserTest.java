package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.*;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class ParserTest {

    @Autowired
    private ParsingService svc;

    @Test
    public void testPreParse() throws IOException {
        File file = ResourceUtils.getFile(this.getClass().getResource("/program (plain).xlsm"));
        System.out.println(file.getAbsolutePath());
        ByteArrayOutputStream os = svc.preParse(new FileInputStream(file));

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.companyName);
    }
}
