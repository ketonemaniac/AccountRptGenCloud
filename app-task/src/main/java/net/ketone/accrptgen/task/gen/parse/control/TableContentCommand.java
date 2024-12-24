package net.ketone.accrptgen.task.gen.parse.control;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Try;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Section;
import net.ketone.accrptgen.task.gen.model.Table;
import net.ketone.accrptgen.task.gen.parse.CellInfo;
import net.ketone.accrptgen.task.util.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

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
                            .orElse(Tuple.of(StringUtils.EMPTY, null))
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
        switch (dataCell.getCellType()) {
            case STRING:
                return dataCell.getStringCellValue();
            case NUMERIC:
                return NumberUtils.numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle());
            case FORMULA:
                return Try.of(() -> NumberUtils.numberFormat(dataCell.getNumericCellValue(), dataCell.getCellStyle()))
                .getOrElse(() -> Try.of(dataCell::getStringCellValue)
                        .getOrElse(dataCell::getCellFormula));
            case BOOLEAN:
                return Boolean.valueOf(dataCell.getBooleanCellValue()).toString();
            default:
//                log.warn("TYPE:" + dataCell.getCellTypeEnum().name());
                return StringUtils.EMPTY;
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
                .map(XSSFCellStyle::getAlignment)
                .ifPresent(a -> cell.setAlignment(alignment(a)));
        // underline
        Optional.ofNullable(style)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getBorderBottom)
                .filter(s -> !s.equals(BorderStyle.NONE))
                .ifPresent(f -> cell.setBottomBorderStyle(cellStyle(f)));
        // see if the cell below has a top border...
        Optional.ofNullable(nextRowStyle)
                .map(XSSFCellStyle.class::cast)
                .map(XSSFCellStyle::getBorderTop)
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
