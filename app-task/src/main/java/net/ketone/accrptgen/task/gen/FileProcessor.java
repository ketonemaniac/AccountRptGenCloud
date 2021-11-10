package net.ketone.accrptgen.task.gen;

import java.io.IOException;

public interface FileProcessor<T> {

    T process(final byte[] input) throws IOException;

}
