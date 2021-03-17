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
                .flatMap(tuple2 -> lineContent(section, cellInfo.getRow(), new Paragraph())
                        .map(tuple2::append))
                .transform(this::transformParagraph)
                .map(Tuple3::_3)
                .doOnNext(section::addSectionElement)
                .map(header -> Tuple.of(section, Boolean.TRUE));
    }

    protected Mono<Tuple3<CellInfo, Section, Paragraph>> transformParagraph(
            final Mono<Tuple3<CellInfo, Section, Paragraph>> tuple3Flux) {
        return tuple3Flux
                .doOnNext(tuple3 -> {
                    if(tuple3._2.getFlags().isInItem()) {
                        tuple3._3.setIndent(tuple3._3.getIndent()+1);
                    }
                });

    }

}
