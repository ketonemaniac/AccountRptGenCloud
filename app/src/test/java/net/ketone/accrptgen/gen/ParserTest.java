package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    private EmailService emailSvc;
    @Autowired
    private StatisticsService statisticsSvc;



    private final String PLAIN_FILENAME = "program (plain) 09.4.18.xlsm";
    private final String TEMPLATE_FILENAME = "All documents 23.4.18.xlsm";

    @Test
    public void testPreParse() throws Exception {
        InputStream inputStream = this.getClass().getResourceAsStream("/" + PLAIN_FILENAME);
        ByteArrayOutputStream os = svc.preParse(inputStream);

        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());

        // TODO: remove, this is integration flow
        ByteArrayOutputStream output = genSvc.generate(data);
        byte[] out = output.toByteArray();
        storageSvc.store(new ByteArrayInputStream(out), "testPreParse.docx");
        // emailSvc.sendEmail(data.getCompanyName(), "testPreParse.docx", new ByteArrayInputStream(out));

    }

    // @Test
    public void testParse() throws IOException {
        InputStream templateStream = storageSvc.load(TEMPLATE_FILENAME);
        XSSFWorkbook templateWb = new XSSFWorkbook(templateStream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        templateWb.write(os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());
        AccountData data = svc.readFile(is);
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());
        ByteArrayOutputStream output =  genSvc.generate(data);
        storageSvc.store(new ByteArrayInputStream(output.toByteArray()), "testParse.docx");
    }



}
