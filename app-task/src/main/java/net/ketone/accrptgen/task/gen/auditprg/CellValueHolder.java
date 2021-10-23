package net.ketone.accrptgen.task.gen.auditprg;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CellValueHolder {

  enum CellType {
        STRING, NUMERIC, DATE
  }

  private CellType cellType;

  private String strVal;

  private Double numVal;

  private Date dateVal;

}
