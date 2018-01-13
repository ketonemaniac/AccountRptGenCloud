package net.ketone.accrptgen.entity;

import java.util.Date;
import java.util.List;

public class AccountData {

    public String companyName;
    public Date generationTime;
    private List<Section> sections;

    public List<Section> getSections() {
        return sections;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }
}
