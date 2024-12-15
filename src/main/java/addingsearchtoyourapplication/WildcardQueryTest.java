package addingsearchtoyourapplication;

import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WildcardQueryTest {
    private Directory directory;

    private void indexSingleFieldDocs(Field[] fields) throws Exception {
        directory = new ByteBuffersDirectory();
        IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(new WhitespaceAnalyzer()));
        for (Field field : fields) {
            Document doc = new Document();
            doc.add(field);
            writer.addDocument(doc);
        }
        writer.commit();
    }

    @Test
    public void wildcard() throws Exception {
        indexSingleFieldDocs(new Field[]{
                new TextField("contents", "wild", Field.Store.YES),
                new TextField("contents", "child", Field.Store.YES),
                new TextField("contents", "mild", Field.Store.YES),
                new TextField("contents", "mildew", Field.Store.YES)
        });


        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        Query query = new WildcardQuery(new Term("contents", "?ild*"));
        TopDocs matches = searcher.search(query, 10);
        assertEquals(3, matches.totalHits.value(), "child, mild, mildew");
        assertEquals(matches.scoreDocs[0].score, matches.scoreDocs[1].score, 0.0f, "score the same");
        assertEquals(matches.scoreDocs[1].score, matches.scoreDocs[2].score, 0.0f, "score the same");
    }

    @Test
    public void fuzzy() throws Exception {
        indexSingleFieldDocs(new Field[]{
                new TextField("contents", "fuzzy", Field.Store.YES),
                new TextField("contents", "wuzzy", Field.Store.YES)
        });
        IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
        Query query = new FuzzyQuery(new Term("contents", "wuzza"));
        TopDocs matches = searcher.search(query, 10);
        Explanation explanation = searcher.explain(query, matches.scoreDocs[0].doc);
        System.out.println(explanation.toString());
        assertEquals(2, matches.totalHits.value(), "both close enough");
        assertTrue(matches.scoreDocs[0].score != matches.scoreDocs[1].score, "different scores");
        StoredFields storedFields = searcher.storedFields();
        assertEquals("wuzzy", storedFields.document(matches.scoreDocs[0].doc).get("contents"), "wuzzy");
        assertEquals("fuzzy", storedFields.document(matches.scoreDocs[1].doc).get("contents"), "fuzzy");
    }
}
