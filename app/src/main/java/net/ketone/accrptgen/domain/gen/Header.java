package net.ketone.accrptgen.domain.gen;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
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
