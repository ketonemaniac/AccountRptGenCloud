package net.ketone.accrptgen.service.gen.auditprg;

import net.ketone.accrptgen.domain.gen.AuditProgrammeMapping;
import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.*;
import java.util.List;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {AuditProgrammeProcessor.class})
public class AuditProgrammeProcessorTest {

    @Autowired
    private AuditProgrammeProcessor processor;

    @MockBean
    private SettingsService configurationService;

    @MockBean
    private StorageService persistentStorage;

    @Mock
    private Properties properties;

    public static final String TEST_OUTPUT = "target/AuditProgrammeProcessorTest.xlsx";

    @Test
    public void testAuditPrgExtract() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        Mockito.when(configurationService.getSettings()).thenReturn(properties);
        Mockito.when(properties.getProperty(SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP))
                .thenReturn("mockAuditPrgTemplate.xlsx");
        File mockAuditPrgTemplate = new File(classLoader.getResource("mockAuditPrgTemplate.xlsx").getFile());
        //XSSFWorkbook mockAuditPrgTemplate = new XSSFWorkbook(new FileInputStream((template)));

        Mockito.when(persistentStorage.loadAsInputStream(any()))
                .thenReturn(new FileInputStream((mockAuditPrgTemplate)));


        List<AuditProgrammeMapping> mappings = List.of(
                AuditProgrammeMapping.builder()
                        .sourceCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet("Sheet1")
                                .cell("A1")
                                .build())
                        .destCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet("MySheet")
                                .cell("A1")
                                .build())
                        .build()
        );
        File template = new File(classLoader.getResource("formulaToText.xlsx").getFile());
        byte[] outBytes = processor.process(mappings, FileUtils.readFileToByteArray(template));

        // visiualize output
        File output= new File(TEST_OUTPUT);
        FileOutputStream out = new FileOutputStream(output);
        out.write(outBytes);
        out.close();
    }

}
