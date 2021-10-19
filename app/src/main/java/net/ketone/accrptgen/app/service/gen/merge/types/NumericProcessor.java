package net.ketone.accrptgen.app.service.gen.merge.types;

import lombok.Getter;
import net.ketone.accrptgen.app.service.gen.merge.CellInfo;
import net.ketone.accrptgen.app.service.gen.merge.MergeUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NumericProcessor implements CellTypeProcessor {

    @Getter
    private CellType cellType = CellType.NUMERIC;

    @Autowired
    private MergeUtils mergeUtils;

    @Override
    public void visit(CellInfo sourceCell, CellInfo targetCell) {
        targetCell.getCell().setCellType(CellType.NUMERIC);
        targetCell.getCell().setCellValue(sourceCell.getCell().getNumericCellValue());
        mergeUtils.setNumericCellStyle(targetCell.getWorkbook(), targetCell.getCell(),
                sourceCell.getCell().getCellStyle());
    }

}
