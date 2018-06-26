package net.ketone.accrptgen.poi;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Independent Tests for Apache POI workings
 */
public class ApachePoiTest {

    @Test
    public void testDecimalPercentages() throws IOException {

        InputStream inputStream = this.getClass().getResourceAsStream("/decimalsPercentages.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        Cell a1 = wb.getSheet("Sheet1").getRow(0).getCell(0);
        Assertions.assertThat(a1.getNumericCellValue()).isEqualTo(0.2);
        System.out.println(a1.getCellStyle().getDataFormat() + " " + a1.getCellStyle().getDataFormatString());
        Cell a2 = wb.getSheet("Sheet1").getRow(1).getCell(0);
        Assertions.assertThat(a2.getNumericCellValue()).isEqualTo(0.1);
        System.out.println(a2.getCellStyle().getDataFormat() + " " + a2.getCellStyle().getDataFormatString());

    }

    @Test
    public void testCopyDecimalPercentages() throws IOException {
        File f = new File("decimalsPercentages-out.xlsx");
        if(f.exists()) f.delete();
        InputStream inputStream = this.getClass().getResourceAsStream("/decimalsPercentages.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(inputStream);
        Cell src = wb.getSheet("Sheet1").getRow(0).getCell(0);
        src.getCellStyle().getDataFormat();

        XSSFWorkbook wb2 = new XSSFWorkbook();
        XSSFSheet sheet = wb2.createSheet("Sheet1");
        sheet.createRow(0);
        Cell tgt = wb2.getSheet("Sheet1").getRow(0).createCell(1);

        tgt.setCellType(CellType.NUMERIC);  // must set cell type before setting value
        tgt.setCellValue(src.getNumericCellValue());
        short df = src.getCellStyle().getDataFormat();
        CellStyle cellStyle = wb2.getStylesSource().createCellStyle();  // must create from styles source
        cellStyle.setDataFormat(df);
        tgt.setCellStyle(cellStyle);

        Cell tgt2 = wb2.getSheet("Sheet1").getRow(0).createCell(2);
        tgt2.setCellType(CellType.NUMERIC);  // must set cell type before setting value
        tgt2.setCellValue(src.getNumericCellValue());

        // check using Excel
        FileOutputStream os = new FileOutputStream("decimalsPercentages-out.xlsx");
        wb2.write(os);

        String output = "";
        if(tgt.getCellStyle().getDataFormat() == 9) {   // 9 means percentage
            NumberFormat myFormatter = new DecimalFormat("##.##%");
            output = myFormatter.format(tgt.getNumericCellValue());
        }
        Assertions.assertThat(output).isEqualTo("20%");
    }

}
