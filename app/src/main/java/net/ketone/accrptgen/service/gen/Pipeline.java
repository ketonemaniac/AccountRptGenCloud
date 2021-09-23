package net.ketone.accrptgen.service.gen;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.config.Constants;
import net.ketone.accrptgen.domain.gen.AuditProgrammeMapping;
import net.ketone.accrptgen.exception.GenerationException;
import net.ketone.accrptgen.service.gen.auditprg.AuditProgrammeMappingExtract;
import net.ketone.accrptgen.service.gen.auditprg.AuditProgrammeProcessor;
import net.ketone.accrptgen.service.gen.merge.TemplateMergeProcessor;
import net.ketone.accrptgen.service.gen.parse.TemplateParseProcessor;
import net.ketone.accrptgen.service.stats.StatisticsService;
import net.ketone.accrptgen.domain.dto.AccountJob;
import net.ketone.accrptgen.domain.gen.AccountData;
import net.ketone.accrptgen.service.mail.Attachment;
import net.ketone.accrptgen.service.mail.EmailService;
import net.ketone.accrptgen.service.store.StorageService;
import net.ketone.accrptgen.service.tasks.TasksService;
import net.ketone.accrptgen.util.ZipUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * File generation batch processes pipeline
 * Done on a background thread
 */
@Slf4j
@Service
@Scope("prototype")
public class Pipeline implements Runnable {

    @Autowired
    private GenerationService generationService;
    @Autowired
    private ParsingService parsingService;
    // in cloud this is just the cache
    @Autowired
    private StorageService tempStorage;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private TasksService tasksService;
    @Autowired
    private TemplateMergeProcessor templateMergeProcessor;
    @Autowired
    private TemplateParseProcessor templateParseProcessor;
    @Autowired
    private AuditProgrammeMappingExtract auditProgrammeMappingExtract;
    @Autowired
    private AuditProgrammeProcessor auditProgrammeProcessor;

    private String filename;
    private String cacheFilename;
    private AccountJob dto;

    public Pipeline(final AccountJob dto) {
        this.cacheFilename = String.valueOf(dto.getFilename());
        this.dto = dto;
    }

    @Override
    public void run() {
        String inputFileName = cacheFilename + ".xlsm";
        log.info("Opening file: " + inputFileName);
        try {
            filename = GenerationService.getFileName(dto.getCompany(), dto.getGenerationTime());

            byte[] workbookArr = tempStorage.load(inputFileName);
            byte[] preParseOutput = templateMergeProcessor.process(workbookArr);
            List<AuditProgrammeMapping> mappings = auditProgrammeMappingExtract.process();

            Attachment inputXlsx = new Attachment(filename + "-plain.xlsm", workbookArr);
            // no need to use the template anymore, delete it.
            tempStorage.delete(inputFileName);

            log.info("template Closing input file stream, " + preParseOutput.length + "_bytes");
            log.info("Start parse operation for " + filename);
            AccountData data = templateParseProcessor.process(preParseOutput);

            data.setGenerationTime(dto.getGenerationTime());
            dto.setProfessionalFees(data.getProcessionalFees());
            dto.setPrevProfessionalFees(data.getPrevProfessionalFees());
            log.info("template finished parsing, sections=" + data.getSections().size());

            // remove sheets and stringify contents
            XSSFWorkbook allDocs = new XSSFWorkbook(new ByteArrayInputStream(preParseOutput));
            Workbook allDocsFinal = parsingService.deleteSheets(
                parsingService.postProcess(allDocs), Arrays.asList(
                        "metadata", "Cover", "Contents", "Control", "Dir info", "Doc list",
                        "Section1", "Section2", "Section3", "Section4", "Section5", "Section6",
                            "Accounts (3)"));
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            allDocsFinal.write(os);

            byte[] generatedDoc = generationService.generate(data);
            log.info("Generated doc. " + generatedDoc.length + "_bytes");

            byte[] generatedAuditProgramme = auditProgrammeProcessor.process(mappings, preParseOutput);

            Attachment doc = new Attachment(filename + ".docx", generatedDoc);
            Attachment auditPrgAttachment = new Attachment(filename + "-auditProgramme.xlsm",
                    generatedAuditProgramme);
            Attachment template = new Attachment(filename + "-allDocs.xlsm", os.toByteArray());
            List<Attachment> attachments = Arrays.asList(doc, template, inputXlsx, auditPrgAttachment);
            emailService.sendEmail(dto, attachments);

            // zip files and store them just in case needed
            Map<String, byte[]> zipInput = attachments.stream()
                    .collect(Collectors.toMap(Attachment::getAttachmentName, Attachment::getData));
            tempStorage.store(ZipUtils.zipFiles(zipInput), filename + ".zip");

            dto.setFilename(filename);
            dto.setStatus(Constants.Status.EMAIL_SENT.name());
            log.info("Updating statistics for " + filename);
            statisticsService.updateTask(dto);
            log.info("Operation complete for " + filename);

        } catch (Throwable e) {
            log.warn("Generation failed", e);
            dto.setFilename(filename);
            dto.setStatus(Constants.Status.FAILED.name());
            try {
                if(e instanceof GenerationException) {
                    dto.setError((GenerationException) e);
                } else {
                    dto.setError(new GenerationException(e));
                }
                statisticsService.updateTask(dto);
            } catch (Throwable e1) {
                log.warn("History file write failed", e1);
            }
        }

    }

}
