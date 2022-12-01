package net.ketone.accrptgen.common.model;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;

/**
 * Exception when input files do not match expected format
 */
@NoArgsConstructor
@Data
public class EvaluationException extends RuntimeException {

    String location;

    public EvaluationException(String stage, Cell cell) {
        super(String.format("%s: Cannot evaluate \"%s\"!%s",
                stage, cell.getSheet().getSheetName(),
                cell.getAddress().formatAsString()));
        this.location = String.format("\"%s\"!%s", cell.getSheet().getSheetName(),
                cell.getAddress().formatAsString());
    }

}
