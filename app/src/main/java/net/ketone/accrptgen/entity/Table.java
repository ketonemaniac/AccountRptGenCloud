package net.ketone.accrptgen.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Table implements SectionElement {

    private List<Integer> columnWidths;
    private int rowHeight;
    private List<List<Cell>> cells;

    public enum BottomBorderStyle {
        NO_LINE,
        SINGLE_LINE,
        DOUBLE_LINE
    }

    @Data
    public class Cell {
        private BottomBorderStyle bottomBorderStyle;
        private String text;
        private boolean isUnderline;
        private boolean isBold;
    }

    public Cell addCell(String text) {
        Cell cell = new Cell();
        cell.setText(text);
        List<Cell> lastRowCells = null;
        if(cells == null) cells = new ArrayList<>();
        if(cells.size() == 0) {
            lastRowCells = new ArrayList<>();
            cells.add(lastRowCells);
        } else {
            lastRowCells = cells.get(cells.size()-1);
            if(lastRowCells.size() == columnWidths.size()) {
                lastRowCells = new ArrayList<>();
                cells.add(lastRowCells);
            }
        }
        lastRowCells.add(cell);
        return cell;
    }
}
