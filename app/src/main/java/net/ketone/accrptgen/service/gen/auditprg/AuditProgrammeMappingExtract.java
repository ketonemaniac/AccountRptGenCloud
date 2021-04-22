package net.ketone.accrptgen.service.gen.auditprg;

import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.gen.AuditProgrammeMapping;
import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static net.ketone.accrptgen.util.ExcelUtils.openExcelWorkbook;

@Slf4j
@Component
public class AuditProgrammeMappingExtract {

    @Autowired
    private StorageService persistentStorage;

    @Autowired
    private SettingsService configurationService;

    /**
     * Maps each row to AuditProgrammeMapping
     * grouping by dest cell
     * reduce the results AuditProgrammeMapping lists to one AuditProgrammeMapping with conbined sourceCell
     * take only the AuditProgrammeMapping lists
     * @return
     * @throws IOException
     */
    public List<AuditProgrammeMapping> process() throws IOException {

        String auditPrgTemplateName = configurationService.getSettings().getProperty(
                SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP);
        log.info("starting fetch audit programme template " + auditPrgTemplateName);
        XSSFWorkbook workbook =
                openExcelWorkbook(persistentStorage.loadAsInputStream(StorageService.AUDIT_PRG_PATH +
                        auditPrgTemplateName));

        return Mono.just(workbook.getSheet("auditPrg"))
                .flatMapMany(sheet -> Flux.range(2, sheet.getLastRowNum())
                        .filter(r -> Optional.ofNullable(sheet.getRow(r)).isPresent())
                        .map(sheet::getRow)
                )
                .map(row -> AuditProgrammeMapping.builder()
                        .sourceCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet(Optional.ofNullable(row.getCell(0)).map(XSSFCell::getStringCellValue)
                                    .orElseThrow())
                                .cell(Optional.ofNullable(row.getCell(1)).map(XSSFCell::getStringCellValue)
                                        .orElseThrow())
                                .build())
                        .destCell(AuditProgrammeMapping.MappingCell.builder()
                                .sheet(Optional.ofNullable(row.getCell(2)).map(XSSFCell::getStringCellValue)
                                        .orElseThrow())
                                .cell(Optional.ofNullable(row.getCell(3)).map(XSSFCell::getStringCellValue)
                                        .orElseThrow())
                                .build())
                        .build()
                )
                .collectList()
                .doOnNext(list -> Try.run(workbook::close).get())
                .block();
    }
}
