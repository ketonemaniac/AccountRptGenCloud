package net.ketone.accrptgen.service.gen.auditprg;

import com.google.common.collect.Streams;
import io.vavr.Tuple;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.gen.AuditProgrammeMapping;
import net.ketone.accrptgen.service.credentials.SettingsService;
import net.ketone.accrptgen.service.store.StorageService;
import net.ketone.accrptgen.util.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.util.ExcelUtils.openExcelWorkbook;
import static net.ketone.accrptgen.util.NumberUtils.numberFormat;

@Slf4j
@Component
public class AuditProgrammeProcessor {

    @Autowired
    private SettingsService configurationService;

    @Autowired
    private StorageService persistentStorage;

    public byte[] process(final List<AuditProgrammeMapping> mappingList, byte[] preParseOutput) throws IOException {

        XSSFWorkbook allDocs = new XSSFWorkbook(new ByteArrayInputStream(preParseOutput));
        
        String auditPrgTemplateName = configurationService.getSettings().getProperty(
                SettingsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP);
        log.info("starting fetch audit programme template " + auditPrgTemplateName);
        XSSFWorkbook auditPrgTemplateWb =
                openExcelWorkbook(persistentStorage.loadAsInputStream(StorageService.AUDIT_PRG_PATH +
                        auditPrgTemplateName));

        Flux.fromIterable(mappingList)
                .map(mapping -> Tuple.of(Optional.ofNullable(mapping.getSourceCell())
                        .map(mappingCell -> {
                            CellReference cr = new CellReference(mappingCell.getCell());
                            Cell c = allDocs.getSheet(mappingCell.getSheet()).getRow(cr.getRow())
                                    .getCell(cr.getCol());
                            return getCellValue(c);
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

        log.debug("start refreshing auditPrgTemplateWb");
        ExcelUtils.evaluateAll(auditPrgTemplateWb, Streams.stream(auditPrgTemplateWb.sheetIterator())
                .collect(Collectors.toList()));
        log.info("auditPrgTemplateWb refreshed. Writing to stream");
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000000);
        log.debug("writing template. os.size()=" + os.size());
        auditPrgTemplateWb.write(os);
        byte [] result = os.toByteArray();
        log.debug("closing template");
        auditPrgTemplateWb.close();

        return result;
    }

    public CellValueHolder getCellValue(Cell c) {
        String dataFormatStr = Optional.ofNullable(c.getCellStyle()).map(CellStyle::getDataFormatString)
                .orElse(StringUtils.EMPTY);
        if(CellType.NUMERIC.equals(c.getCellTypeEnum()) &&
                dataFormatStr.contains("y") && dataFormatStr.contains("m") && dataFormatStr.contains("d")) {
            // this should be a date type
            return CellValueHolder.builder()
                    .cellType(CellValueHolder.CellType.DATE)
                    .dateVal(c.getDateCellValue())
                    .build();
        }
        // numeric or String
        return Try.of(() -> c.getNumericCellValue()).map(val ->
                CellValueHolder.builder()
                        .cellType(CellValueHolder.CellType.NUMERIC)
                        .numVal(val)
                .build())
                .getOrElse(() ->
                        CellValueHolder.builder()
                                .cellType(CellValueHolder.CellType.STRING)
                                .strVal(c.getStringCellValue())
                                .build()
                );
    }
}
