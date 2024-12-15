package addingsearchtoyourapplication;

import common.TestUtil;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    @Test
    public void queryParser() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
            Query query = parser.parse("+JUNIT +ANT -MOCK");
            TopDocs docs = searcher.search(query, 10);
            assertEquals(1, docs.totalHits.value(), "JUnit in Action");
            query = parser.parse("mock OR junit");
            docs = searcher.search(query, 10);
            assertEquals(2, docs.totalHits.value(), "Ant in Action, JUnit in Action");
            dir.close();
        }
    }

    @Test
    public void keyword() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Term t = new Term("isbn", "9781935182023");
            Query query = new TermQuery(t);
            TopDocs docs = searcher.search(query, 10);
            assertEquals(1, docs.totalHits.value(), "JUnit in Action");
            dir.close();
        }
    }

    @Test
    public void termRangeQuery() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = TermRangeQuery.newStringRange("title2", "d", "j", true, true);
            TopDocs docs = searcher.search(query, 10);
            assertEquals(3, docs.totalHits.value());
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                System.out.println(storedFields.document(scoreDoc.doc).get("title2"));
            }
            dir.close();
        }
    }

    @Test
    public void inclusive() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            Query query = IntPoint.newRangeQuery("pubmonth", 200605, 200609);
            TopDocs docs = searcher.search(query, 10);
            assertEquals(1, docs.totalHits.value());
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {

                System.out.println(storedFields.document(scoreDoc.doc).get("title"));
                System.out.println(storedFields.document(scoreDoc.doc).get("pubmonth"));
                System.out.println("------------------");
            }
        }
    }

    @Test
    public void prefix() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            // including subcategories
            Term term = new Term("category", "/technology/computers/programming");
            Query query = new PrefixQuery(term);
            TopDocs docs = searcher.search(query, 10);
            long programmingAndBelow = docs.totalHits.value();
            assertEquals(7, docs.totalHits.value());
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc scoreDoc : docs.scoreDocs) {
                System.out.println(storedFields.document(scoreDoc.doc).get("category"));
            }
            // just programming category
            query = new TermQuery(term);
            docs = searcher.search(query, 10);
            assertEquals(5, docs.totalHits.value());
            long justProgramming = docs.totalHits.value();
            assertTrue(programmingAndBelow >= justProgramming);
        }
    }

    @Test
    public void and() throws Exception {
        Directory dir = TestUtil.getBookIndexDirectory();
        try (DirectoryReader reader = DirectoryReader.open(dir)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TermQuery searchingBooks = new TermQuery(new Term("subject", "search"));
            Query books2010 = IntPoint.newRangeQuery("pubmonth", 201001, 201012);
            BooleanQuery.Builder searchingBooks2010 = new BooleanQuery.Builder();
            searchingBooks2010.add(searchingBooks, BooleanClause.Occur.MUST);
            searchingBooks2010.add(books2010, BooleanClause.Occur.MUST);

            TopDocs docs = searcher.search(searchingBooks2010.build(), 10);
            assertTrue(TestUtil.hitsIncludeTitle(searcher, docs, "Lucene in Action, Second Edition"));
        }
    }
}
