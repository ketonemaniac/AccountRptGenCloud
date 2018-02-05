package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Paragraph;
import net.ketone.accrptgen.entity.Table;

import java.text.SimpleDateFormat;

public interface GenerationService {

    final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

    String generate(AccountData data);

    void write(String sectionName, Paragraph paragraph);

    void write(String sectionName, Table table);
}
