package net.ketone.accrptgen.service.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import net.ketone.accrptgen.config.properties.HeaderProperties;
import net.ketone.accrptgen.domain.gen.Header;
import net.ketone.accrptgen.domain.gen.Paragraph;
import net.ketone.accrptgen.domain.gen.Section;
import net.ketone.accrptgen.service.gen.parse.CellInfo;
import net.ketone.accrptgen.service.gen.parse.Flags;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface ControlCommand {

    String getControlType();

    /**
     * Parse the control command
     * @param cellInfo
     * @param section
     * @return true if it should parse the next line
     */
    Mono<Tuple2<Section, Boolean>> execute(final CellInfo cellInfo, final Section section);

    default Mono<? extends Header.HeaderBuilder<?, ?>> header(final HeaderProperties properties,
                                                        final Section section) {
        return Mono.just(Header.builder())
                .doOnNext(header ->
                        Optional.ofNullable(properties.getHeaders())
                                .map(map -> map.get(section.getName()))
                                .ifPresent(props -> {
                                    header.hasCompanyName(props.getCompanyName());
                                    header.underline(props.getUnderline());
                                }));
    }


    default <T extends Paragraph.ParagraphBuilder<?, ?>> Mono<T> lineContent(Section section, Row row, T p) {

        StringBuffer sb = new StringBuffer();

        return Flux.range(0, section.getControlColumn())
                .map(i -> Tuple.of(i, row.getCell(i),
                        Try.of(() -> Optional.ofNullable(row.getCell(i))
                        .map(Cell::getStringCellValue)
                        .orElse(StringUtils.EMPTY))
                        .getOrElse(StringUtils.EMPTY))
                )
                .skipWhile(tuple -> tuple._3.equalsIgnoreCase(StringUtils.EMPTY))
                .filter(tuple -> tuple._3.equalsIgnoreCase(StringUtils.EMPTY))
                .switchIfEmpty(Flux.just(Tuple.of(0, null, StringUtils.EMPTY)))
                .switchOnFirst(((signal, tuple3Flux) -> {
                    p.indent(signal.get()._1);
                    return tuple3Flux;
                }))
                .doOnNext(tuple -> {
                    sb.append(tuple._3);
                    Optional.ofNullable(tuple._2)
                            .map(Cell::getCellStyle)
                            .map(XSSFCellStyle.class::cast)
                            .map(XSSFCellStyle::getFont)
                            .map(XSSFFont::getBold)
                            .ifPresent(p::isBold);
                })
                .last()
                .doOnNext(last -> p.text(sb.toString().trim()))
                .then(Mono.just(p));
    }

}
