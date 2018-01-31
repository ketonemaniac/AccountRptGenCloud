package net.ketone.accrptgen.entity;

import lombok.Data;

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

    private List<SectionElement> elements;

    public void addSectionElement(SectionElement ele) {
        if(elements == null) {
            elements = new ArrayList<>();
        }
        elements.add(ele);
    }


}
