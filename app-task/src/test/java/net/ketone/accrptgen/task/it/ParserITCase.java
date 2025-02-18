package net.ketone.accrptgen.task.it;

import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import net.ketone.accrptgen.task.config.properties.ParseProperties;
import net.ketone.accrptgen.task.gen.GenerationService;
import net.ketone.accrptgen.task.gen.ParsingService;
import net.ketone.accrptgen.task.gen.generate.BannerService;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.model.SectionElement;
import net.ketone.accrptgen.task.gen.model.Table;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * You need at least TWO files in the /local/files/ folder
 * 1. credentials.properties
 * 2. All documents.xlsm
 * Run with VM options
 * -Dspring.profiles.active=has_template
 */
@IfProfileValue(name = "spring.profiles.active", values = {"itcase"})
@ExtendWith(SpringExtension.class)
@Import(ParsingService.class)
//@ActiveProfiles("local")
//@SpringBootTest
public class ParserITCase {

    @MockitoBean
    StorageService storageService;

    @MockitoBean
    BannerService bannerService;

    @Autowired
    private ParsingService svc;
//    @Autowired
//    private GenerationService genSvc;
//    @Autowired
//    private StorageService storageSvc;
//    @Autowired
//    private EmailService emailSvc;
//    @Autowired
//    private StatisticsService statisticsSvc;


//    @Value("${template.filename}")
//    private String TEMPLATE_FILENAME;
//
//    @Value("${plain.filename}${plain.filename.extension}")
//    private String PLAIN_FILENAME;

//    @Test
//    public void testPreParse() throws Exception {
//        InputStream in = this.getClass().getClassLoader()
//                .getResourceAsStream(PLAIN_FILENAME);
//        XSSFWorkbook workbook = new XSSFWorkbook(in);
//        byte[] preParseOutput = svc.preParse(workbook);
//        AccountData data = svc.readFile(preParseOutput);
//        assertThat(data.getCompanyName()).isEqualTo("MOP ENTERTAINMENT LIMITED");
//
//        for(Section section : data.getSections()) {
//            switch (section.getName()) {
//                case "Section3":
//                    testSection3(section); break;
//                case "Section4":
//                    testSection4(section); break;
//                case "Section5":
//                    testSection5(section); break;
//                case "Section6":
//                    testSection6(section); break;
//            }
//        }
//
//        // TODO: remove, this is integration flow
//        // data.setGenerationTime(new Date());
//        // byte[] out = genSvc.generate(data);
//    }

    private void testSection6(Section section) {
        List<SectionElement> t = section.getElements().stream().filter(s -> s instanceof Table).collect(Collectors.toList());
        Assertions.assertThat(t.size()).isGreaterThanOrEqualTo(4);
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
        Assertions.assertThat(t).isNotNull();
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
        Assertions.assertThat(t).isNotNull();
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
        Assertions.assertThat(t).isNotNull();
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
    public void testStringifyContents() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("formulaToText.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        ExcelTaskUtils.evaluateAll("test", workbook); // svc.postProcess(workbook, new ParseProperties());
        // number
        Cell c = workbook.getSheet("Sheet1").getRow(0).getCell(0);
        assertThat(c.getCellType()).isEqualTo(CellType.NUMERIC);
        assertThat(c.getNumericCellValue()).isEqualTo(5);
        // string
        c = workbook.getSheet("Sheet1").getRow(1).getCell(0);
        assertThat(c.getCellType()).isEqualTo(CellType.STRING);
        assertThat(c.getStringCellValue()).isEqualTo("hello world");
        // boolean
        c = workbook.getSheet("Sheet1").getRow(2).getCell(0);
        assertThat(c.getCellType()).isEqualTo(CellType.BOOLEAN);
        assertThat(c.getBooleanCellValue()).isEqualTo(true);
        // date
        c = workbook.getSheet("Sheet1").getRow(3).getCell(0);
        assertThat(c.getCellType()).isEqualTo(CellType.NUMERIC);    // date is stored as numeric
    }

    @Test
    public void testRemoveColors() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("removeColors.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        Workbook outputWb = svc.postProcess(workbook, new ParseProperties());
        Cell cell = outputWb.getSheet("Sheet1").getRow(0).getCell(0);
        XSSFColor color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
        assertThat(color.getIndex()).isEqualTo(IndexedColors.AUTOMATIC.index);
        cell = outputWb.getSheet("Sheet1").getRow(1).getCell(0);
        color = XSSFColor.toXSSFColor(cell.getCellStyle().getFillForegroundColorColor());
        assertThat(cell.getCellStyle().getFillForegroundColorColor()).isNull();
        outputWorkbook(outputWb);
    }

    private void outputWorkbook(Workbook wb) throws IOException {
        wb.write(new FileOutputStream("target/test.xlsx"));
        wb.close();
    }

    @Test
    public void testDeleteSheets() throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("deleteSheet.xlsx");
        XSSFWorkbook workbook = new XSSFWorkbook(in);
        Workbook outputWb = ExcelTaskUtils.deleteSheets(workbook, Arrays.asList("Sheet1", "Sheet2","Sheet4"));
        assertThat(outputWb.getNumberOfSheets()).isEqualTo(1);
        assertThat(outputWb.getSheet("Sheet3")).isNotNull();
    }

//    @Test
//    public void testParse() throws IOException {
//        InputStream templateStream = storageSvc.loadAsInputStream(TEMPLATE_FILENAME);
//        XSSFWorkbook templateWb = new XSSFWorkbook(templateStream);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
//        templateWb.write(os);
//        AccountData data = svc.readFile(os.toByteArray());
//        System.out.println(data.getCompanyName());
//    }


}
