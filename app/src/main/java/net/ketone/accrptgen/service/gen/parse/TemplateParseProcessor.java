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
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static net.ketone.accrptgen.util.ExcelUtils.openExcelWorkbook;

@Component
@Slf4j
public class TemplateParseProcessor {

    @Autowired
    private List<ControlCommand> controlCommands;

    private Map<String, ControlCommand> controlCommandMap;

    @PostConstruct
    public void init() {
        controlCommandMap = controlCommands.stream().collect(Collectors.toMap(
                ControlCommand::getControlType, Function.identity()));
    }

    public <T> void extract(Consumer<T> setterFunction, Function<Cell, Optional<T>> castingFunction,
                            Workbook workbook, String sheet,
                            int row, int col, T defaultValue) {
        setterFunction.accept(Optional.ofNullable(workbook.getSheet(sheet))
                .map(s -> s.getRow(row))
                .map(r -> r.getCell(col))
                .flatMap(castingFunction)
                .orElse(defaultValue));
    }

    public AccountData process(byte[] preParseOutput) throws IOException {

        AccountData data = new AccountData();
        XSSFWorkbook workbook = openExcelWorkbook(preParseOutput);
        extract(data::setCompanyName, cell -> Optional.ofNullable(cell.getStringCellValue()),
                workbook, "Control", 1, 3, "N/A");
        extract(data::setProcessionalFees,
                cell -> Optional.ofNullable(cell.getNumericCellValue()).map(BigDecimal::valueOf),
                workbook, "Control", 0, 3, BigDecimal.ZERO);
        extract(data::setPrevProfessionalFees,
                cell -> Optional.ofNullable(cell.getNumericCellValue()).map(BigDecimal::valueOf),
                workbook, "Control", 0, 4, BigDecimal.ZERO);
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
                        .orElse(
                                tuple2._2.getFlags().isInTable() ?
                                        controlCommandMap.get(Paragraph.TABLE_TEXT) :
                                        controlCommandMap.get(Paragraph.TEXT))))
                .concatMap(tuple3 -> tuple3._3.execute(tuple3._1, tuple3._2))
                .takeUntil(tuple2 -> !tuple2._2)
                .blockLast()
        ;
    }

    private int charToIdx(char ctlChar) {
        return Character.getNumericValue(ctlChar) - Character.getNumericValue('A');
    }

}
