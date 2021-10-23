package net.ketone.accrptgen.task.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.parse.CellInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class BodyEndCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.END;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        log.info("End line={}", cellInfo.getRow().getRowNum());
        return Mono.just(Tuple.of(section, Boolean.FALSE));
    }

}
