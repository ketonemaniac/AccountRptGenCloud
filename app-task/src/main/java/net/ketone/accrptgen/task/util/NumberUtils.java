package net.ketone.accrptgen.task.util;

import org.apache.poi.ss.usermodel.CellStyle;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtils {

    public static String numberFormat(double value, CellStyle cellStyle) {
        if(value == 0) {
            return "-";
        }
        if(cellStyle != null && cellStyle.getDataFormat() == 9) {
            // Percentages
            NumberFormat percentageFormatter = new DecimalFormat("###.##%;(###.##%)");
            return percentageFormatter.format(value);
        }
        NumberFormat myFormatter = new DecimalFormat("###,###,###,###,###;(###,###,###,###,###)");
        String output = myFormatter.format(value);
        return output;
    }
}
