package net.ketone.accrptgen.service.gen.merge.types;

import lombok.Getter;
import net.ketone.accrptgen.service.gen.merge.CellInfo;
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
