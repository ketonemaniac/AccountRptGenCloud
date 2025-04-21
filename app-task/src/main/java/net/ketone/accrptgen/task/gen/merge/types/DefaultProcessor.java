package net.ketone.accrptgen.task.gen.merge.types;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.ketone.accrptgen.task.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class DefaultProcessor implements CellTypeProcessor {

    @Getter
    private CellType cellType = CellType._NONE;

    @Override
    public void visit(final CellInfo sourceCell, final CellInfo targetCell) {
        log.info("TYPE:" + sourceCell.getCell().getCellType().name());
        targetCell.getCell().setBlank();
    }

}
