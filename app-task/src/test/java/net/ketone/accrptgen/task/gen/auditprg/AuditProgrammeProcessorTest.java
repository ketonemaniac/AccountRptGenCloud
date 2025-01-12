package net.ketone.accrptgen.task.gen.auditprg;

import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import net.ketone.accrptgen.task.gen.model.AuditProgrammeMapping;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

@ExtendWith(SpringExtension.class)
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
                .thenReturn("auditPrgTarget.xlsx");
        File mockAuditPrgTemplate = new File(classLoader.getResource("auditPrgTarget.xlsx").getFile());
        //XSSFWorkbook mockAuditPrgTemplate = new XSSFWorkbook(new FileInputStream((template)));

        Mockito.when(persistentStorage.loadAsInputStream(ArgumentMatchers.any()))
                .thenReturn(new FileInputStream((mockAuditPrgTemplate)));

        List<AuditProgrammeMapping> mappings = Flux.range(1,5)
                .map(i -> AuditProgrammeMapping.builder()
                        .sourceCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet("Sheet1")
                                .cell("A" + i)
                                .build())
                        .destCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet("MySheet")
                                .cell("A" + i)
                                .build())
                        .build())
                .collectList()
                .block();

        File template = new File(classLoader.getResource("auditPrgSrc.xlsx").getFile());
        XSSFWorkbook outBytes = processor.process(mappings, ExcelTaskUtils.openExcelWorkbook(new FileInputStream(template)));

        // visiualize output
        File output= new File(TEST_OUTPUT);
        FileOutputStream out = new FileOutputStream(output);
        out.write(ExcelTaskUtils.saveExcelToBytes(outBytes));
        out.close();
    }

}
