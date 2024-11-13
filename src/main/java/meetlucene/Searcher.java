package meetlucene;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class Searcher {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java " + Searcher.class.getName() + " <index dir> <query>");
            System.exit(1);
        }
        String indexDir = args[0];
        String query = args[1];
        search(indexDir, query);
    }

    public static void search(String indexDir, String query) {
        try (DirectoryReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir).toPath()))) {
            IndexSearcher searcher = new IndexSearcher(indexReader);
            QueryParser parser = new QueryParser("contents", new StandardAnalyzer());
            Query luceneQuery = parser.parse(query);
            long start = System.currentTimeMillis();
            TopDocs hits = searcher.search(luceneQuery, 10);
            long end = System.currentTimeMillis();

            System.out.println("Found " + hits.totalHits +
                    " document(s) (in " + (end - start) +
                    " milliseconds) that matched query '" + query + "':");

            StoredFields storedFields = searcher.storedFields();
            for (int i = 0; i < hits.scoreDocs.length; i++) {
                Document hitDoc = storedFields.document(hits.scoreDocs[i].doc);
                System.out.println(hitDoc.get("filename"));
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error occurred during search: " + e.getMessage());
        }
    }
}