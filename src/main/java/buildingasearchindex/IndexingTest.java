package buildingasearchindex;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    protected long getHitContent(String fieldName, String searchString) throws IOException {
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        QueryParser parser = new QueryParser(fieldName, new WhitespaceAnalyzer());
        Query query = null;
        try {
            query = parser.parse(searchString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        return searcher.search(query, 1).totalHits.value();
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

    @Test
    public void deleteBeforeCommit() throws IOException {
        try (IndexWriter writer = getWriter()) {
            assertEquals(2, writer.getDocStats().numDocs);
            writer.deleteDocuments(new Term("id", "1"));
            assertEquals(2, writer.getDocStats().maxDoc);
            assertEquals(2, writer.getDocStats().numDocs);

        }
    }

    @Test
    public void deleteAfterCommit() throws IOException {
        try (IndexWriter writer = getWriter()) {
            assertEquals(2, writer.getDocStats().numDocs);
            writer.deleteDocuments(new Term("id", "1"));
            writer.commit();
            assertEquals(1, writer.getDocStats().maxDoc);
            assertEquals(1, writer.getDocStats().numDocs);
        }
    }

    @Test
    public void deleteAfterFlush() throws IOException {
        IndexWriter writer = getWriter();
        assertEquals(2, writer.getDocStats().numDocs);
        writer.deleteDocuments(new Term("id", "1"));
        writer.flush();
        assertEquals(2, writer.getDocStats().maxDoc);
        assertEquals(1, writer.getDocStats().numDocs);

        DirectoryReader reader = DirectoryReader.open(directory);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs hits = searcher.search(new TermQuery(new Term("id", "1")), 1);
        assertEquals(1, hits.totalHits.value());

        // commit the changes
        writer.commit();
        reader = DirectoryReader.open(directory);
        searcher = new IndexSearcher(reader);
        hits = searcher.search(new TermQuery(new Term("id", "1")), 1);
        assertEquals(0, hits.totalHits.value());
    }


    @Test
    public void update() throws IOException {
        assertEquals(1, getHitContent("city", "Amsterdam"));

        IndexWriter writer = getWriter();
        Document doc = new Document();
        doc.add(new StringField("id", "1", Field.Store.YES));
        doc.add(new StoredField("country", "Netherlands"));
        doc.add(new TextField("contents", "Den Haag has a lot of museums", Field.Store.NO));
        doc.add(new TextField("city", "Den Haag", Field.Store.YES));
        writer.updateDocument(new Term("id", "1"), doc);
        writer.commit();

        assertEquals(1, getHitContent("id", "1"));
        assertEquals(0, getHitContent("city", "Amsterdam"));
        assertEquals(1, getHitContent("city", "Den Haag"));
    }
}
