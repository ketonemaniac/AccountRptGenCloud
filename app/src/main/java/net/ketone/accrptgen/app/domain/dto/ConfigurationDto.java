package net.ketone.accrptgen.app.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConfigurationDto {

    private String allDocs;

    private String auditPrg;

    @JsonProperty("dBizFunding")
    private String dBizFunding;

    private String breakdownTabs;

    private List<String> sendTo;

    private List<AuditorDto> auditors;

    @Data
    @Builder
    public static class AuditorDto {

        private String name;

        private String banner;

    }

}
