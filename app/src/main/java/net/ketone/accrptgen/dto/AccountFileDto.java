package net.ketone.accrptgen.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class AccountFileDto {

    public enum Status {
        GENERATING, EMAIL_SENT, FAILED
    }

    private String company;
    // key
    private String filename;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    private Date generationTime;
    private String status;

}
