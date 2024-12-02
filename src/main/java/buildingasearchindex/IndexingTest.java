package buildingasearchindex;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IndexingTest {
    protected String[] ids = {"1", "2"};
    protected String[] unindexed = {"Netherlands", "Italy"};
    protected String[] unstored = {"Amsterdam has lots of bridges", "Venice has lots of canals"};
    protected String[] text = {"Amsterdam", "Venice"};

    private Directory directory;

    @BeforeEach
    protected void setUp() throws Exception {
        directory = new ByteBuffersDirectory();
        try (IndexWriter writer = getWriter()) {
            for (int i = 0; i < ids.length; i++) {
                Document doc = new Document();

                // retrievable and stored but not analyzed
                doc.add(new StringField("id", ids[i], Field.Store.YES));
                // not retrievable but stored
                doc.add(new StoredField("country", unindexed[i]));
                // retrievable and analyzed but not stored
                doc.add(new TextField("contents", unstored[i], Field.Store.NO));
                // retrievable, analyzed, and stored
                doc.add(new TextField("city", text[i], Field.Store.YES));
                writer.addDocument(doc);
            }
            writer.commit();
        }
    }

    private IndexWriter getWriter() throws IOException {
        return new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
    }

    @Test
    public void indexWriter() throws IOException {
        IndexWriter writer = getWriter();
        assertEquals(ids.length, writer.getDocStats().numDocs);
        writer.close();
    }

    @Test
    public void indexReader() throws IOException {
        try (DirectoryReader reader = DirectoryReader.open(directory)) {
            assertEquals(ids.length, reader.maxDoc());
            assertEquals(ids.length, reader.numDocs());
        }
    }
}
