package net.ketone.accrptgen.task.gen.merge.types;

import lombok.Getter;
import net.ketone.accrptgen.task.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

@Component
public class StringProcessor implements CellTypeProcessor {

    @Getter
    private CellType cellType = CellType.STRING;

    @Override
    public void visit(CellInfo sourceCell, CellInfo targetCell) {
        targetCell.getCell().setCellType(CellType.STRING);
        targetCell.getCell().setCellValue(sourceCell.getCell().getStringCellValue());
    }

}
