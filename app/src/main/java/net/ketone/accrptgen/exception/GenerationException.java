package net.ketone.accrptgen.exception;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Exception when input files do not match expected format
 */
@NoArgsConstructor
@Data
public class GenerationException extends RuntimeException {

    private String sheet;

    private String cell;

    private String stage;

    private String reason;

    public GenerationException(final String sheet, final String cell, final String stage, final String reason,
                               final String message, final Throwable cause) {
        super(message, cause);
        this.sheet = sheet;
        this.cell = cell;
        this.stage = stage;
        this.reason = reason;
    }

    public GenerationException(Throwable cause) {
        super(cause);
        this.reason = cause.getMessage();
    }

}
