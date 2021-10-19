package net.ketone.accrptgen.app.service.gen.merge.types;

import net.ketone.accrptgen.app.service.gen.merge.CellInfo;
import org.apache.poi.ss.usermodel.CellType;

public interface CellTypeProcessor {

    CellType getCellType();

    void visit(final CellInfo sourceCell, final CellInfo targetCell);

}
