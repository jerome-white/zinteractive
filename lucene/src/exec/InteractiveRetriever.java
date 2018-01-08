package exec;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.concurrent.Future;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ExecutionException;
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

import util.LogAgent;
import util.TermCollection;
import task.DocumentIndexer;
import task.DocumentUpdater;
import task.DocumentJustifier;
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

        LogAgent.setLevel(Level.ALL);

        ExecutorService executors = Executors.newFixedThreadPool(workers);

        /*
         *
         */
        try {
            Analyzer analyzer = new StandardAnalyzer();
            Directory directory = new NIOFSDirectory(index);

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter writer = new IndexWriter(directory, config);

            QueryParser qp = new QueryParser("text", analyzer);
            Query qry = qp.parse(new String(Files.readAllBytes(query)));

            List<Callable<TermCollection>> phase1 = new ArrayList<>();
            List<Callable<TermCollection>> phase2 = new ArrayList<>();
            List<Callable<Void>> phase3 = new ArrayList<>();
            List<Future<TermCollection>> response;

            SelectionStrategy selector = new SequentialSelector(corpus);

            for (String choice : selector) {
                LogAgent.LOGGER.info(choice);

                /*
                 * Remove documents containing the chosen term.
                 */
                LogAgent.LOGGER.info("INDEX: delete");
                Term term = new Term("text", choice);
                writer.deleteDocuments(term);
                writer.commit();

                /*
                 * Identify documents that require indexing. This is
                 * either the documents that were deleted, or all
                 * documents if nothing has ever been indexed.
                 */
                LogAgent.LOGGER.info("INDEX: justify");

                phase1.clear();
                try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(corpus)) {
                    for (Path file : stream) {
                        DocumentJustifier justification =
                            new DocumentJustifier(file, analyzer, directory);
                        phase1.add(justification);
                    }
                }
                response = executors.invokeAll(phase1);

                /*
                 * Uncrypt and re-write documents (to disk) that
                 * require updating.
                 */
                phase2.clear();
                for (Future<TermCollection> terms : response) {
                    TermCollection collection = terms.get();
                    if (collection != null) {
                        phase2.add(new DocumentUpdater(collection, choice));
                    }
                }
                response = executors.invokeAll(phase2);

                /*
                 * Write files to the index.
                 */
                phase3.clear();
                for (Future<TermCollection> terms : response) {
                    TermCollection collection = terms.get();
                    phase3.add(new DocumentIndexer(collection,choice,writer));
                }
                executors.invokeAll(phase3);

                writer.commit();

                /*
                 * Query
                 */
                LogAgent.LOGGER.info("QUERY");

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
        catch (InterruptedException |
               ParseException |
               ExecutionException ex) {
            throw new UndeclaredThrowableException(ex);
        }

        executors.shutdown();
    }

}
