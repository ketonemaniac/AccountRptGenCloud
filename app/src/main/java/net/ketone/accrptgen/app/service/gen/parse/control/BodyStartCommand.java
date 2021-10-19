package net.ketone.accrptgen.app.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import lombok.Getter;
import net.ketone.accrptgen.app.domain.gen.Header;
import net.ketone.accrptgen.app.domain.gen.Paragraph;
import net.ketone.accrptgen.app.domain.gen.Section;
import net.ketone.accrptgen.app.service.gen.parse.CellInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BodyStartCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.START;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(CellInfo cellInfo, Section section) {
        return Mono.just(section)
                .doOnNext(s -> s.getFlags().setStart(true))
                .flatMap(this::sectionStats)
                .map(s -> Tuple.of(s, Boolean.TRUE));
    }


    private Mono<Section> sectionStats(final Section section) {
        return Flux.fromIterable(Optional.ofNullable(section.getElements())
                .orElse(Collections.emptyList())
                .stream()
                .filter(element -> element instanceof Header)
                .map(Header.class::cast)
                .collect(Collectors.toList()))
                .switchOnFirst((signal, flux) -> {
                    if (signal.hasValue()) {
                        signal.get().setFirstLine(true);
                    }
                    return flux;
                })
                .filter(element -> Optional.ofNullable(element.getText())
                        .map(String::trim)
                        .map(String::length)
                        .orElse(0) > 0)
                .last(new Header())     // dummy header if not present
                .doOnNext(header -> {
                    header.setLastLine(true);
                })
                .then(Mono.just(section));
    }



}
