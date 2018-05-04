package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.admin.StatisticsService;
import net.ketone.accrptgen.dto.AccountFileDto;
import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.mail.Attachment;
import net.ketone.accrptgen.mail.EmailService;
import net.ketone.accrptgen.store.StorageService;
import net.ketone.accrptgen.threading.ThreadingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

/**
 * File generation batch processes pipeline
 * Done on a background thread
 */
@Service
@Scope("prototype")
public class Pipeline implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Pipeline.class);

    @Autowired
    private GenerationService generationService;
    @Autowired
    private ParsingService parsingService;
    @Autowired
    private StorageService storageService;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;
    @Autowired
    private ThreadingService threadingService;

    private String companyName;
    private Date generationTime;
    private String filename;

    public Pipeline(String companyName, Date generationTime, String filename) {
        this.companyName = companyName;
        this.generationTime = generationTime;
        this.filename = filename;
    }

    @Override
    public void run() {
        String inputFileName = filename + ".xlsm";
        String outputDocName = filename + ".docx";
        try {
            InputStream is1 = storageService.load(inputFileName);
            byte[] preParseOutput = parsingService.preParse(is1);
            is1.close();
            // no need to use the template anymore, delete it.
            storageService.delete(inputFileName);
            logger.info("template Closing input file stream, " + preParseOutput.length + "_bytes");
            is1.close();
            logger.info("Start parse operation for " + filename);
            AccountData data = parsingService.readFile(preParseOutput);
            data.setGenerationTime(generationTime);
            logger.info("template finished parsing, sections=" + data.getSections().size());

            byte[] generatedDoc = generationService.generate(data);
            logger.info("Generated doc. " + generatedDoc.length + "_bytes");
            Attachment doc = new Attachment(outputDocName, generatedDoc);
            Attachment template = new Attachment(inputFileName, preParseOutput);
            emailService.sendEmail(companyName, Arrays.asList(doc, template));

            AccountFileDto dto = new AccountFileDto();
            dto.setCompany(companyName);
            dto.setFilename(outputDocName);
            dto.setGenerationTime(generationTime);
            dto.setStatus(AccountFileDto.Status.EMAIL_SENT.name());
            logger.info("Updating statistics for " + filename);
            statisticsService.updateAccountReport(dto);
            logger.info("Operation complete for " + filename);

        } catch (Exception e) {
            logger.warn("Generation failed", e);
            throw new RuntimeException(e);
        }

    }

}
