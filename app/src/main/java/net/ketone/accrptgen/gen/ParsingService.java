package net.ketone.accrptgen.gen;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.*;

@Component
public class ParsingService {

    public AccountData readFile(InputStream excelFile) throws IOException {

        AccountData data = new AccountData();
        Workbook workbook = new XSSFWorkbook(excelFile);
        Sheet datatypeSheet = workbook.getSheet("Control");

        // this is D5, put as Row 5 Column D (0 = A1)
        String companyName = datatypeSheet.getRow(1).getCell(3).getStringCellValue();
        System.out.println(companyName);
        data.companyName = companyName;
        return data;
    }

}
