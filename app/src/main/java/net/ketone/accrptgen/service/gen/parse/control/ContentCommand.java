package net.ketone.accrptgen.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.control.Try;
import lombok.Getter;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import net.ketone.accrptgen.service.gen.parse.Flags;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ContentCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.TEXT;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(final CellInfo cellInfo, final Section section) {
        return Mono.just(Tuple.of(cellInfo, section))
                .filter(tuple -> tuple._2.getFlags().isStart())
                .filter(tuple2 -> this.includeRow(tuple2._1, tuple2._2))
                .flatMap(tuple2 -> lineContent(section, cellInfo.getRow(), Paragraph.builder())
                        .map(tuple2::append))
                .doOnNext(tuple3 -> {
                    if(tuple3._2.getFlags().isInItem()) {
                        tuple3._3.indent(tuple3._3.build().getIndent());
                    }
                })
                .map(Tuple3::_3)
                .map(Paragraph.ParagraphBuilder::build)
                .doOnNext(section::addSectionElement)
                .map(header -> Tuple.of(section, Boolean.TRUE));
    }

    private boolean includeRow(final CellInfo cellInfo, final Section section) {
        return Try.of(() -> Optional.ofNullable(cellInfo.getRow())
                .map(row -> row.getCell(section.getYesNoColumn()))
                .map(Cell::getStringCellValue)
                .orElse(Paragraph.NO))
        .getOrElse(Paragraph.NO)
        .equalsIgnoreCase(Paragraph.YES);
    }



    public static void main(String [] args) {
        Flux.empty()
                .blockLast();
    }
}
