package net.ketone.accrptgen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class AccountFileDto implements Serializable {

    public enum Status {
        PENDING, GENERATING, EMAIL_SENT, FAILED
    }

    private String company;
    // key
    private String filename;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    private Date generationTime;
    private String status;

    private String handleName;      // For GCloudStandard, ID in queue
}
