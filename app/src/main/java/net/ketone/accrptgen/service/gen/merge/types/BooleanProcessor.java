package net.ketone.accrptgen.service.gen.merge.types;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.service.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BooleanProcessor implements CellTypeProcessor {

    @Getter
    private CellType cellType = CellType.BOOLEAN;

    @Override
    public void visit(final CellInfo sourceCell, final CellInfo targetCell) {
        targetCell.getCell().setCellType(CellType.BOOLEAN);
        targetCell.getCell().setCellValue(sourceCell.getCell().getBooleanCellValue());
    }

}
