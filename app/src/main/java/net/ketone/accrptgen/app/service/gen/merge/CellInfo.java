package net.ketone.accrptgen.app.service.gen.merge;

import lombok.Builder;
import lombok.Data;
import net.ketone.accrptgen.app.service.gen.parse.Flags;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Data
@Builder
public class CellInfo {

    private XSSFWorkbook workbook;

    private Sheet sheet;

    private Cell cell;

    private FormulaEvaluator evaluator;

    private Flags flags;

}
