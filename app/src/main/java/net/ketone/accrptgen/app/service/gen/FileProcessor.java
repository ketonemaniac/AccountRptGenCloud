package net.ketone.accrptgen.app.service.gen;

import java.io.IOException;

public interface FileProcessor<T> {

    T process(final byte[] input) throws IOException;

}
