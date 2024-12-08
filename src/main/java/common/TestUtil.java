package common;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class TestUtil {
    public static Directory getBookIndexDirectory() throws IOException {
        return FSDirectory.open(new File("index").toPath());
    }
}
