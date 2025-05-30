package net.ketone.accrptgen.task;

import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.constants.Constants;
import net.ketone.accrptgen.common.domain.stats.StatisticsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import net.ketone.accrptgen.task.config.properties.GenerateAFSProperties;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Generate tabs based on "Schedule" column of B6.1 B6.2 tabs
 * the first letter of the schedule (e.g. "F") will determine which set of tabs to copy:
 * - "F1" in schedule will create (or retain) sheets F1, F1.1
 * - "F2" in schedule will create sheets F2, F2.1
 * Double repeated letter sheets (e.g. "RR") are fixed output sheets, should retain
 * Also got a list of fixed sheets, should retain
 */
@Slf4j
@Component
public class GenerateAFSTask {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private GenerateAFSProperties properties;
    @Autowired
    private EmailService emailService;
    @Autowired
    private StatisticsService statisticsService;

    public void run(AccountJob accountJob, byte[] workbookArr) {
        try {
            doRun(accountJob, workbookArr);
            log.info("Updating statistics for {}", accountJob.getFilename());
            statisticsService.updateTask(accountJob);
            log.info("Operation complete for {}", accountJob.getFilename());
        } catch (Throwable e) {
            log.warn("Generation failed", e);
            accountJob.setStatus(Constants.Status.FAILED.name());
            try {
                accountJob.setErrorMsg(e.getMessage());
                statisticsService.updateTask(accountJob);
            } catch (Throwable e1) {
                log.warn("History file write failed", e1);
            }
        }
    }

    public void doRun(AccountJob accountJob, byte[] workbookArr) throws Exception {
        XSSFWorkbook workbook  = ExcelTaskUtils.openExcelWorkbook(workbookArr);

        List<String> auditSheets = ExcelTaskUtils.matchSheetsWithRegex(workbook, properties.getAuditSheets());

        //- Only leave the 2nd gen tabs
        for(String auditSheetName : auditSheets) {
            log.info("revealing sheet {}", auditSheetName);
            workbook.setSheetVisibility(workbook.getSheetIndex(auditSheetName), SheetVisibility.VISIBLE);
        }

        // hide all other sheets
        workbook.sheetIterator().forEachRemaining(sheet -> {
            if(!auditSheets.contains(sheet.getSheetName())) {
                log.info("deep hiding sheet {}", sheet.getSheetName());
                workbook.setSheetVisibility(workbook.getSheetIndex(sheet.getSheetName()), SheetVisibility.VERY_HIDDEN);
            }
        });


        byte[] preParseOutput = ExcelTaskUtils.saveExcelToBytes(workbook);

        List<Attachment> attachments = List.of(new Attachment(accountJob.getFilename(), preParseOutput));
        emailService.sendEmail(accountJob, attachments, properties.getMail().toBuilder().subjectPrefix("AFS Sheets Generation").build());

        tempStorage.store(preParseOutput, accountJob.getFilename());
        accountJob.setStatus(Constants.Status.GENERATED.name());
    }

}
