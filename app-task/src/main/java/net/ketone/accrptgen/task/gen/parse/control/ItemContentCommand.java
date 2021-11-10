package net.ketone.accrptgen.task.gen.parse.control;

import io.vavr.Tuple3;
import lombok.Getter;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.parse.CellInfo;
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
