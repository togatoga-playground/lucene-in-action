package addingsearchtoyourapplication;

import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

public class Explainer {
    public static void main(String[] args) throws Exception{
        if (args.length != 2) {
            System.err.println("Usage: Explainer <index dir> <query>");
            System.exit(1);
        }

        String indexDir = args[0];
        String queryExpression = args[1];
        try (Directory directory = FSDirectory.open(new File(indexDir).toPath())) {
            QueryParser parser = new QueryParser("contents", new SimpleAnalyzer());
            Query query = parser.parse(queryExpression);
            System.out.println("Query: " + queryExpression);

            IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
            TopDocs topDocs = searcher.search(query, 10);
            StoredFields storedFields = searcher.storedFields();
            for (ScoreDoc match: topDocs.scoreDocs) {
                Explanation explanation = searcher.explain(query, match.doc);
                System.out.println("------------------");
                System.out.println(storedFields.document(match.doc).get("title"));
                System.out.println(explanation.toString());
            }
        }

    }
}
