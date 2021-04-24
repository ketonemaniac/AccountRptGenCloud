package net.ketone.accrptgen.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import net.ketone.accrptgen.exception.GenerationException;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Document(collection = "accountJob")
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AccountJob implements Serializable {

    @Id
    private UUID id;

    private String company;

    private String filename;

    private String period;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
    @Temporal( TemporalType.TIMESTAMP )
    private LocalDateTime generationTime;

    private String status;

    private String referredBy;

    private String submittedBy;

    private String handleName;      // For GCloudStandard, ID in queue

    private GenerationException error;

}
