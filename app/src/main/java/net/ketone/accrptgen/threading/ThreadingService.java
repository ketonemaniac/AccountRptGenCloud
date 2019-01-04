package net.ketone.accrptgen.threading;

import net.ketone.accrptgen.dto.AccountFileDto;

import java.io.IOException;
import java.util.Date;

public interface ThreadingService {

    AccountFileDto runPipeline(Date generationTime) throws IOException;

}
