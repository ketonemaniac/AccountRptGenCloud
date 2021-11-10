package net.ketone.accrptgen.task.gen.merge;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

@Component
public class MergeUtils {

    /**
     * keep style such as percentages
     * @param srcCellStyle
     * @param templateWb
     * @param templateCell
     */
    public void setNumericCellStyle(XSSFWorkbook templateWb, Cell templateCell, CellStyle srcCellStyle) {
        short df = srcCellStyle.getDataFormat();
        if(df == 170) df = 14;  // I don't want the d/m/yyyy format
        CellStyle tgtCellStyle = templateCell.getCellStyle();
        if(tgtCellStyle == null) {
            tgtCellStyle = templateWb.getStylesSource().createCellStyle();  // must create from styles source
            templateCell.setCellStyle(tgtCellStyle);
        }
        tgtCellStyle.setDataFormat(df);
    }

}
