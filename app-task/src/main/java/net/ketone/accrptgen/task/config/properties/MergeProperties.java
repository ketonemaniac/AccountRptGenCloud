package net.ketone.accrptgen.task.config.properties;

import lombok.Data;

import java.util.List;

@Data
public class MergeProperties {

    private List<String> preParseSheets;

    private String templatePath;

    private String templateFileProperty;

    private List<String> mergeCellColors;

    private String keepFormulaColor;

}
