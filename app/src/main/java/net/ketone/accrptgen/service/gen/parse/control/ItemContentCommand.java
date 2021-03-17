package net.ketone.accrptgen.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import lombok.Getter;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ItemContentCommand extends ContentCommand {

    @Getter
    private String controlType = Paragraph.ITEM;

    @Override
    protected Mono<Tuple3<CellInfo, Section, Paragraph>> transformParagraph(
            final Mono<Tuple3<CellInfo, Section, Paragraph>> tuple3Flux) {
        return tuple3Flux
                .doOnNext(tuple3 -> {
                    tuple3._3.setItem(true);
                    tuple3._2.getFlags().setInItem(true);
                });

    }

}
