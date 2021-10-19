package net.ketone.accrptgen.app.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.app.config.properties.HeaderProperties;
import net.ketone.accrptgen.app.domain.gen.Paragraph;
import net.ketone.accrptgen.app.domain.gen.Section;
import net.ketone.accrptgen.app.service.gen.parse.CellInfo;
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
    private String controlType = Paragraph.AUDITOR_FOOTER;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return header(properties, section)
            .doOnNext(header -> Optional.ofNullable(cellInfo.getRow())
                .map(row -> row.getCell(0))
                .map(Cell::getStringCellValue)
                .ifPresent(header::setAuditorAddress))
            .doOnNext(section::addSectionElement)
            .map(header -> Tuple.of(section, Boolean.TRUE));
    }
}
