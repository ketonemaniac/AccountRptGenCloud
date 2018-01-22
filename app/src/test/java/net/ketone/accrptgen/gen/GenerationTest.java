package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Paragraph;
import net.ketone.accrptgen.entity.Section;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import static org.mockito.Matchers.any;


@RunWith(SpringRunner.class)
@ActiveProfiles("local")
@SpringBootTest
public class GenerationTest {

    // TODO: use profiles
    @Autowired
    @Qualifier("generationServiceApachePOI")
    private GenerationService svc;

    @MockBean
    StorageService storageService;

    @Before
    public void init() throws IOException {
        Mockito.when(storageService.store(any(XWPFDocument.class), any(String.class))).thenAnswer(new Answer<String>() {
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
    }


    @Test
    public void testGeneration() {
        AccountData data = new AccountData();
        data.setCompanyName("KEITH ENTERPRISES LTD");
        data.setGenerationTime(new Date());

        Section s = new Section();
        data.addSection(s);
        s.setName("SECTION1");
        Paragraph p = new Paragraph();
        p.setText("This is line 1");
        Paragraph p2 = new Paragraph();
        p2.setText("This is line 2");
        s.addParagraph(p);
        s.addParagraph(p2);

        svc.generate(data);

    }
}
