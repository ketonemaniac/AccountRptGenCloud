package net.ketone.accrptgen.task.excelextract;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class ExcelExtractTask {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;

    public void doExcelExtract(final AccountJob job) {
        String inputFileName = job.getFilename();
        log.info("Opening file: " + inputFileName);
        try {
            byte[] workbookArr = tempStorage.load(inputFileName);

            Attachment template = new Attachment(inputFileName + ".OUT", workbookArr);
            List<Attachment> attachments = Arrays.asList(template);
            emailService.sendEmail(job, attachments);

            // no need to use the template anymore, delete it.
            tempStorage.delete(inputFileName);

            job.setStatus(Constants.Status.EMAIL_SENT.name());
            statisticsService.updateTask(job);
            log.info("Operation complete");

        } catch (Exception e) {
            log.error("DIE", e);
        }
    }
}
