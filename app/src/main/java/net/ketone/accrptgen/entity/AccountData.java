package net.ketone.accrptgen.entity;

import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class AccountData {

    public static List<String> SECTION_LIST = ImmutableList.of("Cover", "Contents", "Section1",
            "Section2", "Section3", "Section4", "Section5" , "Section6");

    private String companyName;
    private Date generationTime;
    private List<Section> sections;

    public void addSection(Section s) {
        if(sections == null) {
            sections = new ArrayList<>();
        }
        sections.add(s);
    }
}
