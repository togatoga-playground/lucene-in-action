package common;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;

public class CreateTestIndex {
    public static Document getDocument(String rootDir, File file) throws IOException {
        Properties props = new Properties();
        props.load(new FileInputStream(file));

        Document doc = new Document();
        // category comes from relative path below the base directory
        String category = file.getParent().substring(rootDir.length());
        category = category.replace(File.separatorChar, '/');

        String isbn = props.getProperty("isbn");
        String title = props.getProperty("title");
        String author = props.getProperty("author");
        String url = props.getProperty("url");
        String subject = props.getProperty("subject");

        String pubmonth = props.getProperty("pubmonth");
        System.out.println(title + "\n" + author + "\n" + subject + "\n" + pubmonth + "\n" + category + "\n---------");

        doc.add(new StringField("isbn", isbn, Field.Store.YES));
        doc.add(new StringField("category", category, Field.Store.YES));
        doc.add(new TextField("title", title, Field.Store.YES));
        doc.add(new StringField("title2", title.toLowerCase(), Field.Store.YES));

        // split multiple authors into unique field instances
        String[] authors = author.split(",");
        for (String a : authors) {
            doc.add(new StringField("author", a, Field.Store.YES));
        }

        doc.add(new StoredField("url", url));
        doc.add(new TextField("subject", subject, Field.Store.YES));
        doc.add(new IntField("pubmonth", Integer.parseInt(pubmonth), Field.Store.YES));

        try {
            Date d = DateTools.stringToDate(pubmonth);
            doc.add(new IntPoint("pubmonthAsDay", (int) (d.getTime() / (1000 * 3600 * 24))));
        } catch (ParseException pe) {
            throw new RuntimeException(pe);
        }


        for (String text : new String[] {title, subject, author, category}) {
            doc.add(new TextField("contents", text, Field.Store.NO));
        }

        return doc;
    }
    private static List<File> findFiles(File dir) {
        List<File> results = new ArrayList<File>();
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                results.addAll(findFiles(file));
            } else if (file.getName().endsWith(".properties")) {
                results.add(file);
            }
        }
        return results;
    }
    public static void main(String[] args) throws IOException {
        String dataDir = "data";
        String indexDir = "index";
        List<File> results = findFiles(new File(dataDir));
        System.out.println(results.size() + " books to index");
        try (Directory directory = FSDirectory.open(new File(indexDir).toPath())) {
            IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
            IndexWriter w = new IndexWriter(directory, config);
            for (File file : results) {
                Document doc = getDocument(dataDir, file);
                w.addDocument(doc);
            }
            w.close();
        }

    }
}
