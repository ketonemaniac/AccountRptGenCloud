package net.ketone.accrptgen.app.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.domain.gen.Paragraph;
import net.ketone.accrptgen.app.domain.gen.Section;
import net.ketone.accrptgen.app.service.gen.parse.CellInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class TableEndCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.TABLE_END;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(final CellInfo cellInfo, final Section section) {
        return Mono.just(section)
                .doOnNext(s -> s.getFlags().setInTable(false))
                .map(table -> Tuple.of(section, Boolean.TRUE));
    }

}
