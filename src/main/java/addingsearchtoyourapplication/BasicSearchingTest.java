package addingsearchtoyourapplication;

import common.TestUtil;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BasicSearchingTest {

    @Test
    public void term() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term t = new Term("subject", "ant");
            Query query = new TermQuery(t);
            TopDocs docs = searcher.search(query, 10);
            assertEquals(1, docs.totalHits.value(), "Ant in Action");
            t = new Term("subject", "junit");
            docs = searcher.search(new TermQuery(t), 10);
            assertEquals(2, docs.totalHits.value(), "Ant in Action, JUnit in Action");
            dir.close();
        }

    }
}