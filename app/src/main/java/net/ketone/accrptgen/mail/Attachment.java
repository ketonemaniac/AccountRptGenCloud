package net.ketone.accrptgen.mail;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Attachment {

    private String attachmentName;
    private byte[] data;

}
