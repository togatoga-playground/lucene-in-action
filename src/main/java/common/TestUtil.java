package common;

import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

public class TestUtil {
    public static Directory getBookIndexDirectory() throws IOException {
        return FSDirectory.open(new File("index").toPath());
    }

    public static boolean hitsIncludeTitle(IndexSearcher searcher, TopDocs hits, String title) throws IOException {
        StoredFields storedFields = searcher.storedFields();
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            if (storedFields.document(hits.scoreDocs[i].doc).get("title").equals(title)) {
                return true;
            }
        }
        System.out.println("title '" + title + "' not found");
        return false;
    }
}
