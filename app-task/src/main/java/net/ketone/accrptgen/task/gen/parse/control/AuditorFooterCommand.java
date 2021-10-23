package net.ketone.accrptgen.task.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.parse.CellInfo;
import org.apache.poi.ss.usermodel.Cell;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
public class AuditorFooterCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.AUDITOR_FOOTER;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return header(section)
            .doOnNext(header -> Optional.ofNullable(cellInfo.getRow())
                .map(row -> row.getCell(0))
                .map(Cell::getStringCellValue)
                .ifPresent(header::setAuditorAddress))
            .doOnNext(section::addSectionElement)
            .map(header -> Tuple.of(section, Boolean.TRUE));
    }
}
