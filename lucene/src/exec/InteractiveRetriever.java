package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.TermCollection;
import task.DocumentIndexer;

public class InteractiveRetriever {
    public static void main(String[] args) {
        /*
         *
         */
        Path query = Paths.get(args[0]);
        Path corpus = Paths.get(args[1]);
        Path index = Paths.get(args[2]);
        Path relevance = Paths.get(args[3]);
        int count = Integer.parseInt(args[4]);
        int workers = Integer.parseInt(args[5]);

        /*
         *
         */
        int procs = Runtime.getRuntime().availableProcessors();
        if (workers > procs) {
            workers = procs;
        }

        ExecutorService executors = Executors.newFixedThreadPool(workers);

        /*
         *
         */
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = new NIOFSDirectory(index);
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(directory, config);

            List<Callable<String>> tasks = new LinkedList<Callable<String>>();

            TermCollection queryTerms = new TermCollection(query);
        
            while (true) {
                Collection<String> alterations = new ArrayList<String>();
            
                /*
                 * Update documents and (re-)index
                 */
                try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(corpus)) {
                    tasks.clear();                
                    for (Path file : stream) {
                        DocumentIndexer docind =
                            new DocumentIndexer(file, writer, alterations);
                        tasks.add(docind);
                    }
                }
                executors.invokeAll(tasks);
                writer.commit();

                /*
                 * Query
                 */
                IndexReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);

                QueryParser qp = new QueryParser("text", analyzer);
                Query qry = qp.parse(queryTerms.toString());
                TopDocs hits = searcher.search(qry, count);

                int i = 1;
                for (ScoreDoc hit : hits.scoreDocs) {
                    Document doc = searcher.doc(hit.doc);
                    StringJoiner result = new StringJoiner(" ");
                    result
                        .add(String.valueOf(i))
                        .add(doc.get("docno"))
                        .add(String.valueOf(hit.score));
                    System.out.println(result.toString());
                    i += 1;
                }

                break;
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        catch (InterruptedException | ParseException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();
    }
    
}
