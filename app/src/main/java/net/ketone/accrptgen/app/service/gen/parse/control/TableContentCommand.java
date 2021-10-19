package net.ketone.accrptgen.app.service.gen.parse.control;

import io.netty.util.internal.StringUtil;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.app.domain.gen.Paragraph;
import net.ketone.accrptgen.app.domain.gen.Section;
import net.ketone.accrptgen.app.domain.gen.Table;
import net.ketone.accrptgen.app.service.gen.parse.CellInfo;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static net.ketone.accrptgen.app.util.NumberUtils.numberFormat;

@Slf4j
@Component
public class TableContentCommand implements ControlCommand {

    @Getter
    private String controlType = Paragraph.TABLE_TEXT;

    @Override
    public Mono<Tuple2<Section, Boolean>> execute(final CellInfo cellInfo, final Section section) {

        Table currTable = section.getElements().stream()
                .filter(ele -> ele instanceof Table)
                .map(Table.class::cast)
                .reduce((a,b) -> b)
                .get();
        return Mono.just(section)
                .filter(tuple2 -> this.includeRow(cellInfo, section))
                .flatMapMany(s -> this.tableText(section, cellInfo, currTable))
                .collectList()
                .filter(list -> !list.isEmpty())
                .doOnNext(currTable::addCellRow)
                .map(table -> Tuple.of(section, Boolean.TRUE));
    }


    private Flux<Table.Cell> tableText(final Section section, final CellInfo cellInfo, final Table currTable) {
        return Flux.range(0, currTable.getColumnWidths().size())
        .map(i ->  Optional.ofNullable(cellInfo.getRow())
                            .map(r -> r.getCell(i))
                            .map(cell -> Tuple.of(cellText(cell), cell.getCellStyle()))
                            .orElse(Tuple.of(StringUtil.EMPTY_STRING, null))
            // find also style of the cell in the next row
            .append(Optional.ofNullable(cellInfo.getSheet())
                    .map(s -> s.getRow(cellInfo.getRow().getRowNum()+1))
                    .map(r -> r.getCell(i))
                    .map(Cell::getCellStyle)
                    .orElse(null))
        )
                .map(tuple -> {
                    Table.Cell cell = new Table.Cell();
                    cell.setText(tuple._1);
                    return cellStyle(tuple._2, tuple._3, cell);
                });
    }

    private String cellText(final Cell dataCell) {
        switch (dataCell.getCellTypeEnum()) {
            case STRING:
                return dataCell.getStringCellValue();
            case NUMERIC:
                return numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle());
            case FORMULA:
                return Try.of(() -> numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle()))
                .getOrElse(() -> Try.of(dataCell::getStringCellValue)
                        .getOrElse(dataCell::getCellFormula));
            case BOOLEAN:
                return Boolean.valueOf(dataCell.getBooleanCellValue()).toString();
            default:
//                log.warn("TYPE:" + dataCell.getCellTypeEnum().name());
                return StringUtil.EMPTY_STRING;
        }
    }

    private Table.Cell cellStyle(final CellStyle style,
                                 final CellStyle nextRowStyle,
                                 final Table.Cell cell) {
        Optional.ofNullable(style)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getFont)
                .map(XSSFFont::getBold)
                .ifPresent(cell::setBold);
        Optional.ofNullable(style)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getFont)
                .map(XSSFFont::getUnderline)
                .ifPresent(underline -> cell.setUnderline(underline == FontUnderline.SINGLE.getByteValue()));
        // horizontal alignment
        Optional.ofNullable(style)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getAlignmentEnum)
                .ifPresent(a -> cell.setAlignment(alignment(a)));
        // underline
        Optional.ofNullable(style)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getBorderBottomEnum)
                .filter(s -> !s.equals(BorderStyle.NONE))
                .ifPresent(f -> cell.setBottomBorderStyle(cellStyle(f)));
        // see if the cell below has a top border...
        Optional.ofNullable(nextRowStyle)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getBorderTopEnum)
                .filter(s -> !s.equals(BorderStyle.NONE))
                .ifPresent(f -> cell.setBottomBorderStyle(cellStyle(f)));
        return cell;
    }


    private Table.Alignment alignment(final HorizontalAlignment alignment) {
        switch(alignment) {
            case LEFT:
                return Table.Alignment.LEFT;
            case CENTER:
                return Table.Alignment.CENTER;
            case RIGHT:
                return Table.Alignment.RIGHT;
        }
        return Table.Alignment.LEFT;
    }


    private Table.BottomBorderStyle cellStyle(final BorderStyle f) {
            switch(f) {
                case THIN:
                    return Table.BottomBorderStyle.SINGLE_LINE;
                case DOUBLE:
                    return Table.BottomBorderStyle.DOUBLE_LINE;
                default:
                    return Table.BottomBorderStyle.NO_LINE;
        }
    }

}
