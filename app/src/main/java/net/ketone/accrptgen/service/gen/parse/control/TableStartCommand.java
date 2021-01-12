package net.ketone.accrptgen.service.gen.parse.control;

import com.google.api.client.util.Lists;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.domain.gen.Table;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TableStartCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.TABLE_START;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(final CellInfo cellInfo, final Section section) {
        return Mono.just(section)
                .doOnNext(s -> s.getFlags().setInTable(true))
                .flatMap(s -> this.tableRows(section, cellInfo))
                .doOnNext(section::addSectionElement)
                .map(table -> Tuple.of(section, Boolean.TRUE));
    }


    private Mono<Table> tableRows(final Section section, final CellInfo cellInfo) {
        Table table = new Table();
        return Flux.range(0, section.getControlColumn())
                .flatMap(j -> Try.of(() -> Optional.ofNullable(cellInfo.getRow())
                        .map(r -> r.getCell(j))
                        .map(Cell::getNumericCellValue)
                        .map(Double::intValue)
                        .map(Mono::just)
                        .orElse(Mono.empty())
                    )
                        .onFailure(e -> log.warn(
                                "Unparsable Column Width cell section={} row={} col={} err={}",
                                section.getName(), cellInfo.getRow().getRowNum(), j, e.getMessage()))
                        .getOrElse(Mono.empty())
                )
                .collectList()
                .doOnNext(table::setColumnWidths)
                .then(Try.of(() -> Optional.ofNullable(cellInfo.getRow())
                        .map(r -> r.getCell(section.getYesNoColumn()))
                        .map(Cell::getNumericCellValue)
                        .map(Double::intValue)
                        .map(Mono::just)
                        .orElse(Mono.just(10)))
                    .onFailure(e -> log.warn(
                            "Unparsable Row Height section={} row={} err={}",
                            section.getName(), cellInfo.getRow().getRowNum(), e.getMessage()))
                    .getOrElse(Mono.just(10))   // default height
                )
                .doOnNext(table::setRowHeight)
                .then(Mono.just(table));
    }

}
