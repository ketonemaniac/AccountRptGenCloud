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
import java.util.Date;

@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class ParserTest {

    @Autowired
    private ParsingService svc;
    @Autowired
    private GenerationService genSvc;

    @Test
    public void testPreParse() throws IOException {
//        File file = ResourceUtils.getFile(this.getClass().getResource("/program (plain).xlsm"));
//        System.out.println(file.getAbsolutePath());
        InputStream inputStream = this.getClass().getResourceAsStream("/program (plain).xlsm");
        ByteArrayOutputStream os = svc.preParse(inputStream);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());

        // TODO: remove, this is integration flow
        genSvc.generate(data);
    }



}
