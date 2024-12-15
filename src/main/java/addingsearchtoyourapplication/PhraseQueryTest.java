package addingsearchtoyourapplication;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PhraseQueryTest {
    private Directory dir;
    private IndexSearcher searcher;

    @BeforeEach
    public void setUp() throws IOException {
        dir = new ByteBuffersDirectory();
        IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(new WhitespaceAnalyzer()));
        Document doc = new Document();
        doc.add(new TextField("field", "the quick brown fox jumped over the lazy dog", TextField.Store.YES));
        writer.addDocument(doc);

        writer.close();
        searcher = new IndexSearcher(DirectoryReader.open(dir));
    }

    @AfterEach
    public void tearDown() throws IOException {
        dir.close();
    }

    private boolean matched(String[] phrase, int slop) throws IOException {
        PhraseQuery query = new PhraseQuery(slop, "field", phrase);
        TopDocs matches = searcher.search(query, 10);
        if (matches.totalHits.value() > 0) {
            Explanation explanation = searcher.explain(query, matches.scoreDocs[0].doc);
            System.out.println(explanation.toString());
        }
        return matches.totalHits.value() > 0;
    }

    @Test
    public void slopComparison() throws Exception {
        String[] phrase = new String[] {"quick", "fox"};

        assertFalse(matched(phrase, 0), "exact phrase not found");
        assertTrue(matched(phrase, 1), "close enough");
    }

    @Test
    public void reverse() throws Exception {
        String[] phrase = new String[] {"fox", "quick"};
        assertFalse(matched(phrase, 2), "hop flop");
        assertTrue(matched(phrase, 3), "hop hop slop");
    }

    @Test
    public void multiple() throws Exception {
        assertFalse(matched(new String[] {"quick", "jumped", "lazy"}, 3), "not close enough");
        assertTrue(matched(new String[] {"quick", "jumped", "lazy"}, 4), "just enough slop");

        assertFalse(matched(new String[] {"lazy", "jumped", "quick"}, 7), "almost but not quite");
        assertTrue(matched(new String[] {"lazy", "jumped", "quick"}, 8), "bingo");

        assertTrue(matched(new String[] {"over", "jumped"}, 2), "hop hop slop");
    }
}
