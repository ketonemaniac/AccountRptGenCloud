package net.ketone.accrptgen.app.domain.gen;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(toBuilder = true)
public class AuditProgrammeMapping {

    private MappingCell destCell;

    private MappingCell sourceCell;

    @Data
    @Builder
    public static class MappingCell {

        private String sheet;

        private String cell;

    }

}
