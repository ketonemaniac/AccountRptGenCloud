package net.ketone.accrptgen.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigurationDto {

    private String allDocs;

    private String auditPrg;

    private List<String> sendTo;

    private List<AuditorDto> auditors;

    @Data
    @Builder
    public static class AuditorDto {

        private String name;

        private String banner;

    }

}
