package net.ketone.accrptgen.task.gen.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Header extends Paragraph {

    public enum Underline {
        NO_UNDERLINE,
        BEFORE_LAST,
        AFTER_LAST
    }

    private Underline underline;

    private boolean firstLine;
    private boolean lastLine;
    private boolean hasCompanyName;

    private String companyName;
    private String auditorName;
    private String auditorAddress;
}
