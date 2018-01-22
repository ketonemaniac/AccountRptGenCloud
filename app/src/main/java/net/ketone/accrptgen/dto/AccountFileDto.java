package net.ketone.accrptgen.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AccountFileDto {

    private String company;
    private String filename;
    private String password;
    private Date generationTime;

}
