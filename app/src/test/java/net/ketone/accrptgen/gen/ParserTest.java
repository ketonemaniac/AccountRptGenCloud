package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.util.POILogger;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.*;
import java.util.Date;

/**
 * Run with VM options
 * -Dpoi.log.level=1 -Dorg.apache.poi.util.POILogger=org.apache.poi.util.SystemOutLogger
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class ParserTest {

    @Autowired
    private ParsingService svc;
    @Autowired
    private GenerationService genSvc;
    @Autowired
    private StorageService storageSvc;

    private final String PLAIN_FILENAME = "program (plain) 19.1.18.xlsm";
    private final String TEMPLATE_FILENAME = "All documents 10.3.18.xlsm";

    @Test
    public void testPreParse() throws IOException {
        InputStream inputStream = this.getClass().getResourceAsStream("/" + PLAIN_FILENAME);
        ByteArrayOutputStream os = svc.preParse(inputStream);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());

        // TODO: remove, this is integration flow
        genSvc.generate(data);
    }

    // @Test
    public void testParse() throws IOException {
        XSSFWorkbook templateWb = storageSvc.getTemplate(TEMPLATE_FILENAME);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        templateWb.write(os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());
        genSvc.generate(data);
    }



}
