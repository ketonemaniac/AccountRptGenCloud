package net.ketone.accrptgen.common.model;

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

    public GenerationException(final String sheet, final String cell, final String stage, final String msg) {
        super(msg);
        this.sheet = sheet;
        this.cell = cell;
        this.stage = stage;
    }

    public GenerationException(Throwable cause) {
        super(String.format("%s:%s", cause.getClass().getName(), cause.getMessage()));
    }

}
