package net.ketone.accrptgen.gen;

import net.ketone.accrptgen.entity.AccountData;

import java.text.SimpleDateFormat;

public interface GenerationService {

    final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");

    String generate(AccountData data);

}
