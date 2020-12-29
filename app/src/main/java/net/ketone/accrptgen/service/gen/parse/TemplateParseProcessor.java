package net.ketone.accrptgen.service.gen.parse;

import io.vavr.Tuple;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.gen.AccountData;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.FileProcessor;
import net.ketone.accrptgen.service.gen.parse.control.ControlCommand;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.SynchronousSink;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TemplateParseProcessor implements FileProcessor<AccountData> {

    @Autowired
    private List<ControlCommand> controlCommands;

    private Map<String, ControlCommand> controlCommandMap;

    @PostConstruct
    public void init() {
        controlCommandMap = controlCommands.stream().collect(Collectors.toMap(
                ControlCommand::getControlType, Function.identity()));
    }


    public String extractCompanyName(Workbook workbook) throws IOException {
        Sheet controlSheet = workbook.getSheet("Control");
        // this is D5, put as Row 5 Column D (0 = A1)
        return controlSheet.getRow(1).getCell(3).getStringCellValue();
    }

    @Override
    public AccountData process(byte[] preParseOutput) throws IOException {

        AccountData data = new AccountData();
        XSSFWorkbook workbook = openExcelWorkbook(preParseOutput);

        data.setCompanyName(extractCompanyName(workbook));
        Sheet metadataSheet = workbook.getSheet("Metadata");

        int secIdx = 1;
        for (String sheetName : AccountData.SECTION_LIST) {
            int itemIdx = 1;
            Section section = new Section();
            section.setName(sheetName);
            section.setFontSize((int) metadataSheet.getRow(itemIdx++).getCell(secIdx).getNumericCellValue());
            char ctlChar = metadataSheet.getRow(itemIdx++).getCell(secIdx).getStringCellValue().charAt(0);
            section.setControlColumn(charToIdx(ctlChar));
            char yesNoChar = metadataSheet.getRow(itemIdx++).getCell(secIdx).getStringCellValue().charAt(0);
            section.setYesNoColumn(charToIdx(yesNoChar));

            parseSection(workbook, section);
            data.addSection(section);
            secIdx++;
        }
        workbook.close();
        return data;
    }

    private void parseSection(final XSSFWorkbook workbook, final Section section) {
        final Sheet sectionSheet = workbook.getSheet(section.getName());

        log.info("section={}, control={} ", section.getName(), section.getControlColumn());
        AtomicInteger atomicInteger = new AtomicInteger();

        Flux.generate((SynchronousSink<Integer> synchronousSink) ->
                synchronousSink.next(atomicInteger.getAndIncrement()))
                .map(i -> Tuple.of(i, section))
                .map(tuple2 -> Tuple.of(CellInfo.builder()
                        .workbook(workbook)
                        .sheet(sectionSheet)
                        .row(Optional.ofNullable(sectionSheet.getRow(tuple2._1))
                                .orElseThrow(() -> new RuntimeException(String.format("Missing row section=%s row=%s",
                                        tuple2._2.getName(), tuple2._1))))
                        .controlCell(Optional.ofNullable(sectionSheet.getRow(tuple2._1))
                                .map(row -> row.getCell(tuple2._2.getControlColumn()))
                                .orElse(null)
                        ).build(), section)
                )
                .map(tuple2 -> tuple2.append(Optional.ofNullable(tuple2._1.getControlCell())
                        .map(Cell::getStringCellValue)
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .map(controlCommandMap::get)
                        .orElse(controlCommandMap.get(Paragraph.TEXT))))
                .flatMap(tuple3 -> tuple3._3.execute(tuple3._1, tuple3._2))
                .takeUntil(tuple2 -> tuple2._2)
                .blockLast()
        ;
    }

    private int charToIdx(char ctlChar) {
        return Character.getNumericValue(ctlChar) - Character.getNumericValue('A');
    }

}
