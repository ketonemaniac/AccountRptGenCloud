package net.ketone.accrptgen.service.gen;

import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.domain.gen.*;
import net.ketone.accrptgen.service.store.FileStorageService;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;

import static net.ketone.accrptgen.config.Constants.CREDENTIALS_FILE;
import static net.ketone.accrptgen.config.Constants.TEMPLATE_FILE;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {GenerationTest.MockStorageConfiguration.class, GenerationServiceApachePOI.class} )
@ActiveProfiles("local")
public class GenerationTest {

    @TestConfiguration
    static class MockStorageConfiguration {

        @Bean
        @Primary
        public StorageService storageService() throws IOException {
            StorageService storageService = Mockito.mock(FileStorageService.class);
            //
            Mockito.when(storageService.store(any(byte[].class), any(String.class))).thenAnswer(new Answer<String>() {
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
            Mockito.when(storageService.loadAsInputStream(eq(CREDENTIALS_FILE))).thenReturn(new ByteArrayInputStream(apiKey.getBytes()));

            InputStream inputStream = this.getClass().getResourceAsStream("/" + TEMPLATE_FILE);
            Mockito.when(storageService.loadAsInputStream(eq(TEMPLATE_FILE))).thenReturn(inputStream);

            return storageService;
        }
    }

    @Autowired
    private GenerationService svc;

    public static final String TEST_OUTPUT = "target/GenerationTest.docx";

    @Before
    public void init() {
        new File(TEST_OUTPUT).delete();
    }

    @Test
    public void testGeneration() throws IOException {
        AccountData data = new AccountData();
        data.setCompanyName("KEITH ENTERPRISES LTD");
        data.setGenerationTime(LocalDateTime.now());

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

        byte[] outBytes = svc.generate(data);
        Assertions.assertThat(outBytes.length).isGreaterThan(0);

        // visiualize output
        File output= new File(TEST_OUTPUT);
        FileOutputStream out = new FileOutputStream(output);
        out.write(outBytes);
        out.close();

        // manual assertion to see if file is valid...
    }
}
