package net.ketone.accrptgen.domain.gen;

import lombok.Data;
import net.ketone.accrptgen.service.gen.parse.Flags;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes one section of the report
 */
@Data
public class Section {

    private String name;

    private int fontSize;

    private int controlColumn;

    private int yesNoColumn;

    private Flags flags = new Flags();

    private List<SectionElement> elements;

    public void addSectionElement(SectionElement ele) {
        if(elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(ele);
    }

}
