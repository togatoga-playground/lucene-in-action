package addingsearchtoyourapplication;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class NearRealTimeTest {

    @Test
    public void nearRealTime() throws Exception {
        try {
            Directory dir = new ByteBuffersDirectory();
            IndexWriter writer = new IndexWriter(dir, new IndexWriterConfig(
                    new StandardAnalyzer()
            ));

            for (int i = 0; i < 10; i++) {
                Document doc = new Document();
                doc.add(new StringField("id", ""+i, Field.Store.NO));
                doc.add(new TextField("text", "aaa", Field.Store.NO));
                writer.addDocument(doc);
            }
            DirectoryReader reader = DirectoryReader.open(writer);
            IndexSearcher searcher = new IndexSearcher(reader);

            Query query = new TermQuery(new Term("text", "aaa"));
            TopDocs docs = searcher.search(query, 1);
            assertEquals(10, docs.totalHits.value(), "should find all docs");
            // delete
            writer.deleteDocuments(new Term("id", "7"));
            // add new doc
            Document doc = new Document();
            doc.add(new StringField("id", "11", Field.Store.NO));
            doc.add(new TextField("text", "bbb", Field.Store.NO));
            writer.addDocument(doc);

            // still find deleted doc
            assertEquals(1, searcher.search(new TermQuery(new Term("id", "7")), 1).totalHits.value(), "should not find a deleted doc");
            // not find new docs
            assertEquals(0, searcher.search(new TermQuery(new Term("text", "bbb")), 1).totalHits.value(), "should not find a new doc");

            // near-real-time search
            IndexReader newReader = DirectoryReader.openIfChanged(reader);
            assertNotEquals(reader, newReader, "should be different readers");
            reader.close();
            searcher = new IndexSearcher(newReader);

            // not found deleted doc
            assertEquals(0, searcher.search(new TermQuery(new Term("id", "7")), 1).totalHits.value(), "should not find a deleted doc");
            // found new docs
            assertEquals(1, searcher.search(new TermQuery(new Term("text", "bbb")), 1).totalHits.value(), "should find a new doc");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
