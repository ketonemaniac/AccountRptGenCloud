package net.ketone.accrptgen.task.gen.parse;

import lombok.Data;

@Data
public class Flags {

    private boolean start = false;

    private boolean inTable = false;

    private boolean inItem = false;

}
