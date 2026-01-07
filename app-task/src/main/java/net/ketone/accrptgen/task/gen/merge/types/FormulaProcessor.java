package net.ketone.accrptgen.task.gen.merge.types;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.common.model.EvaluationException;
import net.ketone.accrptgen.task.gen.merge.CellInfo;
import net.ketone.accrptgen.task.gen.merge.MergeUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Copy the source cell to the target cell.
 * NO EVALUATION -- assume user opens the Excel and saves calculated formulae
 */
@Component
@Slf4j
public class FormulaProcessor implements CellTypeProcessor {

    @Getter
    private CellType cellType = CellType.FORMULA;

    @Autowired
    private MergeUtils mergeUtils;

    @Override
    public void visit(final CellInfo sourceCell, final CellInfo targetCell) {
        log.debug("Formula Sheet=" + sourceCell.getSheet().getSheetName() +
                " Cell=" + sourceCell.getCell().getAddress().formatAsString());
        // templateCell.setCellFormula(cell.getCellFormula());
        try {
//            CellValue cellValue = sourceCell.getEvaluator().evaluate(sourceCell.getCell());
            switch(sourceCell.getCell().getCachedFormulaResultType()) {
                case NUMERIC:
                    targetCell.getCell().setCellType(CellType.NUMERIC);
                    targetCell.getCell().setCellValue(sourceCell.getCell().getNumericCellValue());
                    mergeUtils.setNumericCellStyle(targetCell.getWorkbook(), targetCell.getCell(),
                            sourceCell.getCell().getCellStyle());
                    log.info("input cell with formula: " + sourceCell.getCell().getCellFormula() + " is now: "
                            + targetCell.getCell().getNumericCellValue() + " of type NUMERIC");
                    targetCell.getCell().setCellFormula(null);
                    break;
                default:
                        // try String for anything else
                        targetCell.getCell().setCellType(CellType.STRING);
                        targetCell.getCell().setCellValue(sourceCell.getCell().getStringCellValue());
                        log.info("input cell with formula: " + sourceCell.getCell().getCellFormula() + " is now: "
                                + targetCell.getCell().getStringCellValue() + " of type STRING.");
                        targetCell.getCell().setCellFormula(null);
                break;
            }
//            if(CellType.ERROR.equals(cellValue.getCellType())) {
//                throw new RuntimeException();
//            }
        } catch (Exception err) {
            throw new EvaluationException("FormulaProcessor",sourceCell.getCell());
        }
    }

}
