package net.ketone.accrptgen.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Document(collection = "accountJob")
@Data
public class AccountJob implements Serializable {

    @Id
    private UUID id;

    private String company;

    private String filename;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS", timezone = "Asia/Shanghai")
    @Temporal( TemporalType.TIMESTAMP )
    private Date generationTime;
    private String status;
    private String referredBy;
    private String submittedBy;

    private String handleName;      // For GCloudStandard, ID in queue
}
