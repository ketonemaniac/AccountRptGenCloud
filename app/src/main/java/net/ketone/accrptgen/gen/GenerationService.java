package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;
import net.ketone.accrptgen.entity.Paragraph;
import net.ketone.accrptgen.entity.Table;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public interface GenerationService {

    static String getFileName(String companyName, Date generationTime) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        return companyName + "-" + sdf.format(generationTime);
    }

    byte[] generate(AccountData data) throws IOException;

    void write(String sectionName, Paragraph paragraph);

    void write(String sectionName, Table table);
}
