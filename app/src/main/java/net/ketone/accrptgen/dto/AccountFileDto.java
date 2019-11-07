package net.ketone.accrptgen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class AccountFileDto implements Serializable {

    private UUID id;

    private String company;

    private String filename;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    private Date generationTime;
    private String status;
    private String referredBy;
    private String submittedBy;

    private String handleName;      // For GCloudStandard, ID in queue
}
