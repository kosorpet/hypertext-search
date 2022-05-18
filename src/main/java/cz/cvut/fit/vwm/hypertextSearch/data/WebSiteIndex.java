package cz.cvut.fit.vwm.hypertextSearch.data;


import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import javax.print.Doc;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WebSiteIndex {
    private IndexWriter writer;
    private IndexSearcher searcher;
    private final StandardAnalyzer analyzer;
    private final Directory memoryIndex;

    public WebSiteIndex(LocalDatabase database) {
        //Init indexing tools
        analyzer = new StandardAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        memoryIndex = new RAMDirectory();
        try {
            writer = new IndexWriter(memoryIndex, indexWriterConfig);
        } catch (IOException e){
            e.printStackTrace();
        }

        //Add websites as lucene documents for searching
        for(WebSite site: database.getSites()){
            Document document = new Document();
            document.add(new TextField("url", site.getUrl(), Field.Store.YES));
            document.add(new TextField("body", site.getPlaintext(), Field.Store.YES));
            try {
                writer.addDocument(document);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    public List<ScoreDoc> query(String inField, String queryString) {
        try {
            Query query = new QueryParser(inField, analyzer).parse(queryString);
            writer.commit();
            IndexReader indexReader = DirectoryReader.open(memoryIndex);
            searcher = new IndexSearcher(indexReader);
            //Search for top n matching documents
            TopDocs topDocs = searcher.search(query, 30);
            return Arrays.asList(topDocs.scoreDocs);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Document scoreDoctoDoc(ScoreDoc doc) {
        Document res = new Document();
        try {
            res = searcher.doc(doc.doc);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }
}
