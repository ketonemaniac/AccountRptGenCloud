package net.ketone.accrptgen.task;

import com.google.common.collect.Streams;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.credentials.SettingsService;
import net.ketone.accrptgen.common.mail.Attachment;
import net.ketone.accrptgen.common.mail.EmailService;
import net.ketone.accrptgen.common.model.AccountJob;
import net.ketone.accrptgen.common.store.StorageService;
import net.ketone.accrptgen.common.util.ExcelTaskUtils;
import net.ketone.accrptgen.task.config.properties.BreakdownTabsProperties;
import net.ketone.accrptgen.task.gen.merge.TemplateMergeProcessor;
import net.ketone.accrptgen.task.util.ZipUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.poi.ss.usermodel.Sheet;

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

    /**
     * gets schedule names from Summary sheets (B6.1, B6.2)
     * @param workbook
     * @return
     */
    private Set<String> getScheduleNames(XSSFWorkbook workbook) {
        return properties.getScheduleSheetColumn().entrySet().stream()
                .map(sheetCol -> {
                    Iterator<Row> iterator = workbook.getSheet(sheetCol.getKey()).rowIterator();
                    return StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED), false)
                            .skip(8)
                            .map(row -> row.getCell(colStrMap.indexOf(sheetCol.getValue().charAt(0)), Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
                                    .getStringCellValue().trim())
                            .filter(StringUtils::isNotBlank);
                })
                .reduce(Stream::concat)
                .map(Stream::toList)
                .map(HashSet::new)
                .orElseThrow();
    }

    public void run(AccountJob accountJob, byte[] workbookArr) throws Exception {
        XSSFWorkbook workbook  =templateMergeProcessor.process(workbookArr, properties.getMerge());

        Set<String> sheets = new TreeSet<>(properties.getFixSheets());
        // all double-lettered sheets are fixed
        Set<String> doubleLetteredSheets = Streams.stream(workbook.sheetIterator())
                .map(Sheet::getSheetName)
                .filter(sheetName -> {
                    var p = Pattern.compile("([A-Z])\\1+").matcher("RA1");
                    return p.find();
                })
                .collect(Collectors.toSet());
        sheets.addAll(doubleLetteredSheets);

        // get the schedules which need to be cloned
        Map<String, List<String>> scheduleCategories = getScheduleNames(workbook).stream()
                .filter(scheduleName -> !properties.getBanSheets().contains(scheduleName))
                .filter(scheduleName -> !doubleLetteredSheets.contains(scheduleName))
                .collect(Collectors.groupingBy(scheduleName -> {
                            var a = Pattern.compile("^([A-Z]+)").matcher(scheduleName);
                            return a.find() ? a.group() : scheduleName;
                        }));

        for (Map.Entry<String, List<String>> set : scheduleCategories.entrySet()) {
            List<Sheet> sheetsInCategory = Streams.stream(workbook.sheetIterator())
                    .filter(sheet -> !properties.getBanSheets().contains(sheet.getSheetName()))
                    .filter(sheet -> !properties.getFixSheets().contains(sheet.getSheetName()))
                    .filter(sheet -> !doubleLetteredSheets.contains(sheet.getSheetName()))
                    .filter(sheet -> {
                        var a = Pattern.compile("^([A-Z]+)").matcher(sheet.getSheetName());
                        return a.find() && a.group().equals(set.getKey());
                    })
                    .toList();
            for (int i = 2; i <= set.getValue().size(); i++) {
                final int sheetNum = i;
                // we need to clone extra sheets
                sheetsInCategory.forEach(sheet -> {
                    // see https://medium.com/stackera/java-regex-part-6-group-and-subgroup-2985dc2d42d4
                    // with 1 find(), group(0) is the implicit entire pattern, group (1+) are the explicit (bracketed) patterns
                    var namePattern = Pattern.compile("([A-Z]{1,})[0-9]*(.*)").matcher(sheet.getSheetName());
                    if(namePattern.find()) {
                        String newSheetName =
                                String.format("%s%d%s", namePattern.group(1), sheetNum, namePattern.group(2));
                        log.info("cloning sheet {} as {}", sheet.getSheetName(), newSheetName);
                        workbook.cloneSheet(workbook.getSheetIndex(sheet), newSheetName);
                        workbook.setSheetOrder(newSheetName, workbook.getSheetIndex(sheet.getSheetName()) + set.getValue().size());
                        sheets.add(newSheetName);
                    } else {
                        log.warn("sheet naming problem, not cloned: {}", sheet.getSheetName());
                    }
                });
            }
            sheets.addAll(sheetsInCategory.stream().map(Sheet::getSheetName).toList());
        }
        log.info("All sheets: {}", String.join(",", sheets));

        List<String> sheetsToBeRemoved = Streams.stream(workbook.sheetIterator())
                .map(Sheet::getSheetName)
                .filter(sheetName -> !sheets.contains(sheetName))
                .toList();

        sheetsToBeRemoved.forEach(sheet -> workbook.removeSheetAt(workbook.getSheetIndex(sheet)));

        byte[] preParseOutput = ExcelTaskUtils.saveExcelToBytes(workbook);

        List<Attachment> attachments = List.of(new Attachment(accountJob.getFilename(), preParseOutput));
        emailService.sendEmail(accountJob, attachments, properties.getMail());

        // zip files and store them just in case needed
//        Map<String, byte[]> zipInput = attachments.stream()
//                .collect(Collectors.toMap(Attachment::getAttachmentName, Attachment::getData));
        tempStorage.store(preParseOutput, accountJob.getFilename());
    }

}
