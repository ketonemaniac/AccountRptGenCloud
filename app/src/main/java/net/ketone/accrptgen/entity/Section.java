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

    private List<Paragraph> paragraphs;

    public void addParagraph(Paragraph p) {
        if(paragraphs == null) {
            paragraphs = new ArrayList<>();
        }
        paragraphs.add(p);
    }


}
