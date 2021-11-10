package net.ketone.accrptgen.common.model;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class CellValueHolder {

  public enum CellType {
        STRING, NUMERIC, DATE
  }

  private CellType cellType;

  private String strVal;

  private Double numVal;

  private Date dateVal;

}
