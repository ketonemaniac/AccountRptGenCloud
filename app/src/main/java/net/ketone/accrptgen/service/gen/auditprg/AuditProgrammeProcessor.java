package net.ketone.accrptgen.service.gen.auditprg;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.service.credentials.CredentialsService;
import net.ketone.accrptgen.service.gen.FileProcessor;
import net.ketone.accrptgen.service.store.StorageService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class AuditProgrammeProcessor implements FileProcessor<byte[]> {

    @Autowired
    private CredentialsService credentialsService;

    @Autowired
    private StorageService persistentStorage;

    @Override
    public byte[] process(byte[] input) throws IOException {
        
        String auditPrgTemplateName = credentialsService.getCredentials().getProperty(
                CredentialsService.PREPARSE_AUIDTPRG_TEMPLATE_PROP);
        log.info("starting fetch audit programme template " + auditPrgTemplateName);
        XSSFWorkbook auditPrgTemplateWb = Optional.ofNullable(
                openExcelWorkbook(persistentStorage.loadAsInputStream(auditPrgTemplateName)))
                .orElseThrow(() -> new IOException("Unable to get File " + auditPrgTemplateName));

        // refresh everything
//        log.debug("start refreshing template");
//        evaluateAll(templateWb, templateSheetMap);
//        log.info("template refreshed. Writing to stream");
        ByteArrayOutputStream os = new ByteArrayOutputStream(1000000);
        log.debug("writing template. os.size()=" + os.size());
        auditPrgTemplateWb.write(os);
        byte [] result = os.toByteArray();
        log.debug("closing template");
        auditPrgTemplateWb.close();

        return result;
    }

}
