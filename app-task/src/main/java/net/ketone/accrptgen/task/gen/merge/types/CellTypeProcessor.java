package net.ketone.accrptgen.task.gen.merge.types;

import net.ketone.accrptgen.task.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;

public interface CellTypeProcessor {

    CellType getCellType();

    /**
     * copies sourceCell to targetCell
     * @param sourceCell src
     * @param targetCell tgt
     */
    void visit(final CellInfo sourceCell, final CellInfo targetCell);

}
