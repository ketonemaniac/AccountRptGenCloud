package net.ketone.accrptgen.entity;

import lombok.Data;

/**
 * One paragraph/line of the input Excel
 */
@Data
public class Paragraph {

    // commands
    public static final String AUDITOR_HEADING = "auditor heading";
    public static final String AUDITOR_FOOTER = "auditor footer";
    public static final String HEADING = "heading";
    public static final String HEADING2 = "heading2";
    public static final String START = "start";
    public static final String END = "end";
    public static final String TABLE_START = "table start";
    public static final String TABLE_END = "table end";

    private boolean show;
    private String text;
    private String control;
    private int indent;
    private boolean isBold;

}
