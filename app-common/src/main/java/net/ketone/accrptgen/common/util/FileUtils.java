package net.ketone.accrptgen.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtils {

    public static String uniqueFilename(final String nonUniquePart, final LocalDateTime generationTime) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        return nonUniquePart + "-" + df.format(generationTime);
    }

}
