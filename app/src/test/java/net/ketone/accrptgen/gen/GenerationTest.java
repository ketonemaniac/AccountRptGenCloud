package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.admin.CredentialsService;
import net.ketone.accrptgen.admin.FileBasedCredentialsService;
import net.ketone.accrptgen.entity.*;
import net.ketone.accrptgen.mail.SendgridEmailService;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.Arrays;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;


@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest(classes = {GenerationTest.MockStorageConfiguration.class})
public class GenerationTest {

    @Configuration
    @ComponentScan(basePackages = {"net.ketone.accrptgen"})
    static class MockStorageConfiguration {

        @Bean
        public StorageService storageService() throws IOException {
            StorageService storageService = Mockito.mock(StorageService.class);
            Mockito.when(storageService.store(any(InputStream.class), any(String.class))).thenAnswer(new Answer<String>() {
                @Override
                public String answer(InvocationOnMock invocationOnMock) throws Throwable {
                    XWPFDocument doc = (XWPFDocument) invocationOnMock.getArguments()[0];
                    String filename = (String) invocationOnMock.getArguments()[1];
                    File output= new File(filename);
                    FileOutputStream out = new FileOutputStream(output);
                    System.out.println(output.getAbsolutePath());
                    doc.write(out);
                    out.close();
                    doc.close();
                    return filename;
                }
            });
            String apiKey = CredentialsService.SENDGRID_API_KEY_PROP + "=1234567890";
            Mockito.when(storageService.load(eq(FileBasedCredentialsService.CREDENTIALS_FILE))).thenReturn(new ByteArrayInputStream(apiKey.getBytes()));

            return storageService;
        }
    }


    // TODO: use profiles
    @Autowired
    @Qualifier("generationServiceApachePOI")
    private GenerationService svc;

    @Test
    public void testGeneration() throws IOException {
        AccountData data = new AccountData();
        data.setCompanyName("KEITH ENTERPRISES LTD");
        data.setGenerationTime(new Date());

        Section s = new Section();
        data.addSection(s);
        s.setName("Section1");
        Paragraph p = new Paragraph();
        p.setText("This is line 1");
        Paragraph p2 = new Paragraph();
        p2.setText("This is line 2");
        p2.setBold(true);
        s.addSectionElement(p);
        s.addSectionElement(p2);

        Table t = new Table();
        t.setColumnWidths(Arrays.asList(4000, 2000, 8000));
        t.addCell("A1");
        t.addCell("A2");
        t.addCell("A3");
        t.addCell("B1");
        t.addCell("B2");
        t.addCell("B3");
        s.addSectionElement(t);

        s = new Section();
        data.addSection(s);
        s.setName("Section2");
        p = new Paragraph();
        p.setText("line 1 again");
        p2 = new Paragraph();
        p2.setText("line 2 again");
        s.addSectionElement(p);
        s.addSectionElement(p2);

        Header header1 = new Header();
        header1.setText("COMPANY HEADER");
        s.addSectionElement(header1);
        Header header2 = new Header();
        header2.setText("COMPANY HEADER 2");
        s.addSectionElement(header2);

        svc.generate(data);

    }
}
