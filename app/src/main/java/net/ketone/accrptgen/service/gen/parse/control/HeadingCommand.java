package net.ketone.accrptgen.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.config.properties.HeaderProperties;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class HeadingCommand implements ControlCommand {

    @Autowired
    private HeaderProperties properties;

    @Getter
    private String controlType = Paragraph.HEADING;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return header(properties, section)
                .flatMap(header -> lineContent(section, cellInfo.getRow(), header))
                .map(Header.HeaderBuilder::build)
                .doOnNext(section::addSectionElement)
                .map(header -> Tuple.of(section, Boolean.TRUE));
    }


}
