package net.ketone.accrptgen.task;

import com.google.common.collect.Streams;
import io.vavr.control.Try;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import net.ketone.accrptgen.task.config.properties.BreakdownTabsProperties;
import net.ketone.accrptgen.task.gen.merge.TemplateMergeProcessor;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static net.ketone.accrptgen.common.util.ExcelUtils.colStrMap;

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
public class GenerateTabTask {

    @Autowired
    private StorageService tempStorage;
    @Autowired
    private TemplateMergeProcessor templateMergeProcessor;
    @Autowired
    private BreakdownTabsProperties properties;
    @Autowired
    private EmailService emailService;
    @Autowired
    private SettingsService configurationService;
    @Autowired
    private StorageService persistentStorage;

    private CellAddress findScheduleColumn(XSSFSheet sheet) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(sheet.rowIterator(), Spliterator.ORDERED), false)
                .flatMap(row -> StreamSupport.stream(Spliterators.spliteratorUnknownSize(row.cellIterator(), Spliterator.ORDERED), false))
                .filter(cell -> Try.of(() -> cell.getStringCellValue().equals("Schedule")).getOrElse(false))
                .map(Cell::getAddress)
                .findFirst()
                .orElseThrow();
    }

    /**
     * gets needed schedule names from Summary sheets (B6.1, B6.2)
     * @param workbook
     * @return
     */
    private Set<String> getScheduleSheetNames(XSSFWorkbook workbook) {
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        return properties.getScheduleSheetColumn().entrySet().stream()
                .flatMap(sheetCol -> {
                    XSSFSheet sheet = workbook.getSheet(sheetCol.getKey());
                    CellAddress scheduleCell = findScheduleColumn(sheet);
                    Iterator<Row> iterator = sheet.rowIterator();
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                            .skip(scheduleCell.getRow()+1)
                            .map(row -> row.getCell(scheduleCell.getColumn(), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK))
                            .filter(cell -> cell.getCellType() != CellType.BLANK)
                            .map(cell -> {
                                CellValue cellValue = evaluator.evaluate(cell);
                                return cellValue.getStringValue().trim();
                            })
                            .filter(StringUtils::isNotBlank);
                })
                .collect(Collectors.toSet());
    }

    public void run(AccountJob accountJob, byte[] workbookArr) throws Exception {
        XSSFWorkbook workbook  =templateMergeProcessor.process(workbookArr, properties.getMerge());

        Set<String> sheets = new TreeSet<>(properties.getFixSheets());

        for (String scheduleSheetGroup : getScheduleSheetNames(workbook)) {
            // When it says like "QI1", sub-sheets also need to be cloned, like "QI1.1, QI1.2, QI1.3..... etc"
            // sheetsInCategory gives all sub-sheets in QI:
            List<Sheet> sheetsInCategory = Streams.stream(workbook.sheetIterator())
                    .filter(sheet -> !properties.getBanSheets().contains(sheet.getSheetName()))
                    .filter(sheet -> !properties.getFixSheets().contains(sheet.getSheetName()))
                    .filter(sheet -> {
                        var a = Pattern.compile("^([A-Z]+[0-9]+)").matcher(sheet.getSheetName());
                        return a.find() && a.group().equals(scheduleSheetGroup);
                    })
                    .toList();
            sheets.addAll(sheetsInCategory.stream().map(Sheet::getSheetName).toList());
        }
        log.info("All sheets: {}", String.join(",", sheets));

        List<String> auditSheets = Streams.stream(workbook.sheetIterator())
                .filter(sheet -> {
                    for(String auditSheetName : properties.getAuditSheets()) {
                        var a = Pattern.compile(auditSheetName).matcher(sheet.getSheetName());
                        if(a.find()) return true;
                    }
                    return false;
                })
                .map(Sheet::getSheetName)
                .toList();
        ;
        for(String auditSheetName : auditSheets) {
            log.info("deep hiding sheet {}", auditSheetName);
            workbook.setSheetVisibility(workbook.getSheetIndex(auditSheetName), SheetVisibility.VERY_HIDDEN);
        }

        // unused sheet after X----->
        workbook.setSheetOrder("X------>", workbook.getNumberOfSheets()-1);

        for(String sheetName : Streams.stream(workbook.sheetIterator())
                .map(Sheet::getSheetName)
                .filter(sheetName -> !sheets.contains(sheetName))
                .filter(sheetName -> !auditSheets.contains(sheetName))
                .filter(sheetName -> !"X------>".equals(sheetName))
                .toList()) {
                    log.info("putting sheet at back {}", sheetName);
                    workbook.setSheetOrder(sheetName, workbook.getNumberOfSheets()-1);
        };

        byte[] preParseOutput = ExcelTaskUtils.saveExcelToBytes(workbook);

        List<Attachment> attachments = List.of(new Attachment(accountJob.getFilename(), preParseOutput));
        emailService.sendEmail(accountJob, attachments, properties.getMail());

        // zip files and store them just in case needed
//        Map<String, byte[]> zipInput = attachments.stream()
//                .collect(Collectors.toMap(Attachment::getAttachmentName, Attachment::getData));
        tempStorage.store(preParseOutput, accountJob.getFilename());
    }

//    /**
//     * TODO: remove
//     */
//    private void oldCloneSheetLogic(byte[] workbookArr) throws IOException {
//        XSSFWorkbook workbook  =templateMergeProcessor.process(workbookArr, properties.getMerge());
//        Set<String> sheets = new TreeSet<>(properties.getFixSheets());
//
//        // get the schedules which need to be cloned
//        // unused logic: multiple schedules of the same category could exist.
//        // e.g. "QI1", "QI2" all belong to "QI" (key) with List("QI1", "QI2") (value) to be cloned as
//        // "QI1.1, QI1.2, QI1.3", "QI2.1, QI2.2, QI2.3"
//        Map<String, List<String>> scheduleCategories = getScheduleSheetNames(workbook).stream()
//                .filter(scheduleName -> !properties.getBanSheets().contains(scheduleName))
//                .collect(Collectors.groupingBy(scheduleName -> {
//                    var a = Pattern.compile("^([A-Z]+)").matcher(scheduleName);
//                    return a.find() ? a.group() : scheduleName;
//                }));
//
//        for (Map.Entry<String, List<String>> set : scheduleCategories.entrySet()) {
//            // When it says like "QI1", sub-sheets also need to be cloned, like "QI1.1, QI1.2, QI1.3..... etc"
//            // sheetsInCategory gives all sub-sheets in QI:
//            List<Sheet> sheetsInCategory = Streams.stream(workbook.sheetIterator())
//                    .filter(sheet -> !properties.getBanSheets().contains(sheet.getSheetName()))
//                    .filter(sheet -> !properties.getFixSheets().contains(sheet.getSheetName()))
//                    .filter(sheet -> {
//                        var a = Pattern.compile("^([A-Z]+)").matcher(sheet.getSheetName());
//                        return a.find() && a.group().equals(set.getKey());
//                    })
//                    .toList();
//            for (int i = 2; i <= set.getValue().size(); i++) {
//                final int sheetNum = i;
//                // we need to clone extra sheets
//                sheetsInCategory.forEach(sheet -> {
//                    // see https://medium.com/stackera/java-regex-part-6-group-and-subgroup-2985dc2d42d4
//                    // with 1 find(), group(0) is the implicit entire pattern, group (1+) are the explicit (bracketed) patterns
//                    var namePattern = Pattern.compile("([A-Z]{1,})[0-9]*(.*)").matcher(sheet.getSheetName());
//                    if(namePattern.find()) {
//                        String newSheetName =
//                                String.format("%s%d%s", namePattern.group(1), sheetNum, namePattern.group(2));
//                        log.info("cloning sheet {} as {}", sheet.getSheetName(), newSheetName);
//                        workbook.cloneSheet(workbook.getSheetIndex(sheet), newSheetName);
//                        workbook.setSheetOrder(newSheetName, workbook.getSheetIndex(sheet.getSheetName()) + set.getValue().size());
//                        sheets.add(newSheetName);
//                    } else {
//                        log.warn("sheet naming problem, not cloned: {}", sheet.getSheetName());
//                    }
//                });
//            }
//            sheets.addAll(sheetsInCategory.stream().map(Sheet::getSheetName).toList());
//        }
//        log.info("All sheets: {}", String.join(",", sheets));
//    }
}
