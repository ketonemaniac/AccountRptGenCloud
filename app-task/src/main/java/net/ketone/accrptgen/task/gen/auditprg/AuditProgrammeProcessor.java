package net.ketone.accrptgen.task.gen.auditprg;

import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelUtils;
import net.ketone.accrptgen.task.gen.model.AuditProgrammeMapping;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;


@Slf4j
@Component
public class AuditProgrammeProcessor {

    @Autowired
    private SettingsService configurationService;

    @Autowired
    private StorageService persistentStorage;

    public XSSFWorkbook process(final List<AuditProgrammeMapping> mappingList, XSSFWorkbook allDocs) throws IOException {

        String auditPrgTemplateName = configurationService.getSettings().getProperty(
                SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP);
        log.info("starting fetch audit programme template " + auditPrgTemplateName);
        XSSFWorkbook auditPrgTemplateWb =
                ExcelTaskUtils.openExcelWorkbook(persistentStorage.loadAsInputStream(StorageService.AUDIT_PRG_PATH +
                        auditPrgTemplateName));

        Flux.fromIterable(mappingList)
                .map(mapping -> Tuple.of(Optional.ofNullable(mapping.getSourceCell())
                        .map(mappingCell -> {
                            CellReference cr = new CellReference(mappingCell.getCell());
                            Cell c = allDocs.getSheet(mappingCell.getSheet()).getRow(cr.getRow())
                                    .getCell(cr.getCol());
                            return ExcelUtils.getCellValue(c);
                        }).get(), mapping.getDestCell()))
                .doOnNext(tuple2 -> {
                    CellReference cr = new CellReference(tuple2._2.getCell());
                    Cell c = auditPrgTemplateWb.getSheet(tuple2._2.getSheet()).getRow(cr.getRow())
                            .getCell(cr.getCol());
                    switch(tuple2._1.getCellType()) {
                        case DATE:
                            c.setCellValue(tuple2._1.getDateVal()); break;
                        case NUMERIC:
                            c.setCellValue(tuple2._1.getNumVal()); break;
                        case STRING:
                            c.setCellValue(tuple2._1.getStrVal()); break;
                    }
                }).blockLast();

        return auditPrgTemplateWb;
    }

}
