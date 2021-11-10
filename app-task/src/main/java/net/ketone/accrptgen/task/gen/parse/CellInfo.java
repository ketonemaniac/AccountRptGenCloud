package net.ketone.accrptgen.task.gen.parse;

import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Defines cell coordinates in the source template Excel
 */
@Data
@Builder
public class CellInfo {

    private XSSFWorkbook workbook;

    private Row row;

    private Sheet sheet;

    private Cell controlCell;

}
