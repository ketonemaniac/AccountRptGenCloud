package net.ketone.accrptgen.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.config.properties.HeaderProperties;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import net.ketone.accrptgen.service.gen.parse.Flags;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class AuditorFooterCommand implements ControlCommand {

    @Autowired
    private HeaderProperties properties;

    @Getter
    private String controlType = Paragraph.AUDITOR_HEADING;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return header(properties, section)
            .doOnNext(header -> Optional.ofNullable(cellInfo.getRow())
                .map(row -> row.getCell(0))
                .map(Cell::getStringCellValue)
                .ifPresent(header::auditorAddress))
            .map(Header.HeaderBuilder::build)
            .doOnNext(section::addSectionElement)
            .map(header -> Tuple.of(section, Boolean.TRUE));
    }
}
