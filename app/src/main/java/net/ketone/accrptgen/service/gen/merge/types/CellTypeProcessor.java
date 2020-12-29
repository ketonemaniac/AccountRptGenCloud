package net.ketone.accrptgen.service.gen.merge.types;

import net.ketone.accrptgen.service.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;

public interface CellTypeProcessor {

    CellType getCellType();

    void visit(final CellInfo sourceCell, final CellInfo targetCell);

}
