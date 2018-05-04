package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Paragraph;
import net.ketone.accrptgen.entity.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Locale;

public interface GenerationService {

    final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);

    byte[] generate(AccountData data) throws IOException;

    void write(String sectionName, Paragraph paragraph);

    void write(String sectionName, Table table);
}
