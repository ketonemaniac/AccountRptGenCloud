package net.ketone.accrptgen.service.gen.parse;

import lombok.Data;

@Data
public class Flags {

    private boolean start = false;

    private boolean inTable = false;

    private boolean inItem = false;

}
