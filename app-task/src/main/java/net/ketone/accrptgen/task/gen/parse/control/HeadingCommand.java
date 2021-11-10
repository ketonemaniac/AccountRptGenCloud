package net.ketone.accrptgen.task.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.task.config.properties.ParseProperties;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.parse.CellInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class HeadingCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.HEADING;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return header(section)
                .flatMap(header -> lineContent(section, cellInfo.getRow(), header))
                .doOnNext(section::addSectionElement)
                .map(header -> Tuple.of(section, Boolean.TRUE));
    }


}
