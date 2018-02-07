package net.ketone.accrptgen.entity;

import lombok.Data;

@Data
public class Header extends Paragraph {

    enum Formatting {
        ALL_BOLD,
        LAST_LINE_PLAIN,
        UNDERLINE_THEN_LAST_LINE_PLAIN;
    }

    private Formatting formatting;
    private boolean firstLine;
    private boolean lastLine;
    private String companyName;

}
