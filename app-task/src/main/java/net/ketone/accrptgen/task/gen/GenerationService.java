package net.ketone.accrptgen.task.gen;

import net.ketone.accrptgen.task.gen.model.AccountData;
import net.ketone.accrptgen.task.gen.model.Paragraph;
import net.ketone.accrptgen.task.gen.model.Table;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface GenerationService {

    byte[] generate(AccountData data) throws IOException;

    void write(String sectionName, Paragraph paragraph);

    void write(String sectionName, Table table);
}
