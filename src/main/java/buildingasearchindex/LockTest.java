package buildingasearchindex;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

class LockTest {
    private Directory dir;
    @BeforeEach
    void setUp() throws IOException {
        dir = FSDirectory.open(new File(System.getProperty("java.io.tmpdir") + "/index").toPath());
    }

    @Test
    public void writeLock() throws IOException {
        IndexWriter writer1 = new IndexWriter(dir, new IndexWriterConfig());
        IndexWriter writer2 = null;
        try {
            writer2 = new IndexWriter(dir, new IndexWriterConfig());
            fail("We should never reach this point");
        } catch (IOException e) {
            e.printStackTrace();
        }

        finally {
            writer1.close();
            assertNull(writer2);
        }
    }
}
