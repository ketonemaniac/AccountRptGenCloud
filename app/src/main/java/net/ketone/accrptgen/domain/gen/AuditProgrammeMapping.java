package net.ketone.accrptgen.domain.gen;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder(toBuilder = true)
public class AuditProgrammeMapping {

    private MappingCell destCell;

    @Singular
    private List<MappingCell> sourceCells;

    @Data
    @Builder
    public static class MappingCell {

        private String sheet;

        private String cell;

    }

}
