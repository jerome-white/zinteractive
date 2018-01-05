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
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.TermCollection;
import task.DocumentIndexer;
import select.SelectionStrategy;
import select.SequentialSelector;

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

            TermCollection queryTerms = new TermCollection(query);
            QueryParser qp = new QueryParser("text", analyzer);
            Query qry = qp.parse(queryTerms.toString());

            List<Callable<String>> tasks = new LinkedList<Callable<String>>();

            SelectionStrategy selector = new SequentialSelector(corpus);

            for (Collection<String> choices : selector) {
                /*
                 * Update documents and (re-)index
                 */
                for (String s : choices) {
                    writer.deleteDocuments(new Term("text", s));
                }
                writer.commit();

                tasks.clear();
                try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(corpus)) {
                    for (Path file : stream) {
                        tasks.add(new DocumentIndexer(file, writer, choices));
                    }
                }
                executors.invokeAll(tasks);
                writer.commit();

                /*
                 * Query
                 */
                IndexReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);
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

		// selector.setFeedback();		
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
