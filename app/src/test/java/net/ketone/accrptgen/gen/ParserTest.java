package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Section;
import net.ketone.accrptgen.entity.SectionElement;
import net.ketone.accrptgen.entity.Table;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

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
        byte[] workbookArr = Files.readAllBytes(new File("/" + PLAIN_FILENAME).toPath());
        XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(workbookArr));
        byte[] preParseOutput = svc.preParse(workbook);
        AccountData data = svc.readFile(preParseOutput);
        assertThat(data.getCompanyName()).isEqualTo("MOP ENTERTAINMENT LIMITED");

        for(Section section : data.getSections()) {
            switch (section.getName()) {
                case "Section3":
                    testSection3(section); break;
                case "Section4":
                    testSection4(section); break;
                case "Section5":
                    testSection5(section); break;
                case "Section6":
                    testSection6(section); break;
            }
        }

        // TODO: remove, this is integration flow
        // data.setGenerationTime(new Date());
        // byte[] out = genSvc.generate(data);
    }

    private void testSection6(Section section) {
        List<SectionElement> t = section.getElements().stream().filter(s -> s instanceof Table).collect(Collectors.toList());
        assertTrue(t.size() >= 4);
        Table table = (Table) t.get(3);
        for(List<Table.Cell> row : table.getCells()) {
            // Yes/No column value derived from other sheets
            if(row.get(1).getText().contains("As at")) {
                assertThat(row.get(8).getText()).isEqualTo("9,302,500");
            }
        }
    }

    private void testSection5(Section section) {
        Optional<SectionElement> t = section.getElements().stream().filter(s -> s instanceof Table).findFirst();
        assertTrue(t.isPresent());
        Table table = (Table) t.get();
        for(List<Table.Cell> row : table.getCells()) {
            // formula derived from other sheets
            if(row.get(0).getText().contains("Balance as at")) {
                assertThat(row.get(5).getText()).isEqualTo("(1,527,691)");
            }
        }

    }

    private void testSection3(Section section) {
        Optional<SectionElement> t = section.getElements().stream().filter(s -> s instanceof Table).findFirst();
        assertTrue(t.isPresent());
        Table table = (Table) t.get();
        for(List<Table.Cell> row : table.getCells()) {
            // addition formula
            if(row.get(0).getText().contains("Other income")) {
                assertThat(row.get(4).getText()).isEqualTo("4,605");
            }
            // summation in source and target
            if(row.get(0).getText().contains("from operations and before taxation")) {
                assertThat(row.get(4).getText()).isEqualTo("(1,537,691)");
            }
        }
    }

    private void testSection4(Section section) {
        Optional<SectionElement> t = section.getElements().stream().filter(s -> s instanceof Table).findFirst();
        assertTrue(t.isPresent());
        Table table = (Table) t.get();
        for(List<Table.Cell> row : table.getCells()) {
            // cell formulas on source
            if(row.get(0).getText().contains("Property, plant and equipment")) {
                assertThat(row.get(2).getText()).isEqualTo("1,540,797");
            }
            // cell formula on source and target
            if(row.get(0).getText().contains("Amount due from a director")) {
                assertThat(row.get(2).getText()).isEqualTo("20,000");
                assertThat(row.get(4).getText()).isEqualTo("20,000");
            }
        }
    }



    @Test
    public void testParse() throws IOException {
        InputStream templateStream = storageSvc.loadAsInputStream(TEMPLATE_FILENAME);
        XSSFWorkbook templateWb = new XSSFWorkbook(templateStream);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        templateWb.write(os);
        AccountData data = svc.readFile(os.toByteArray());
        System.out.println(data.getCompanyName());

        data.setGenerationTime(new Date());
        byte[] output =  genSvc.generate(data);
        // storageSvc.store(new ByteArrayInputStream(output), "testParse.docx");
    }



}
