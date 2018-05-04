package net.ketone.accrptgen.threading;

import java.util.Date;

public interface ThreadingService {

    void runPipeline(String companyName, Date generationTime, String filename);

}
