package org.corpustograph.indexing;

import java.io.IOException;
import java.nio.file.Path;

public interface DocumentReader {
    String read(Path path) throws IOException;
}
