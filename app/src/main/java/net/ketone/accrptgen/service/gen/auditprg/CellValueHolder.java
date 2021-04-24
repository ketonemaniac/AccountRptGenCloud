package net.ketone.accrptgen.service.gen.auditprg;

import com.google.appengine.api.datastore.DatastoreTimeoutException;
import io.vavr.control.Try;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;

import java.util.Date;
import java.util.Optional;

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
