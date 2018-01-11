package exec;

import java.io.PrintStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.sql.Timestamp;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutorService;
import java.lang.AutoCloseable;
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
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import task.AssemblerException;
import task.DocumentIndexer;
import task.DocumentUpdater;
import task.DocumentJustifier;
import task.DocumentPipeline;
import select.SelectionStrategy;
import select.SequentialSelector;

public class InteractiveRetriever implements AutoCloseable {
    private IndexWriter writer;
    private ExecutorService executors;
    private Query query;
    private Directory directory;

    public InteractiveRetriever(Path query,
                                Path index,
                                int workers) {
        executors = Executors.newFixedThreadPool(workers);

        LogAgent.LOGGER.fine("INITIALIZE: Lucene");

        Analyzer analyzer = new WhitespaceAnalyzer();
        try {
            directory = new NIOFSDirectory(index);

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(directory, config);
            writer.commit();

            QueryParser qp = new QueryParser(DocumentIndexer.CONTENT,
                                             analyzer);
            this.query = qp.parse(new String(Files.readAllBytes(query)));
        }
        catch (ParseException e) {
            throw new UndeclaredThrowableException(e);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void close() {
        executors.shutdown();
    }

    public int index(Path corpus, String choice) {
        try {
            purge(choice);
            return add(corpus, choice);
        }
        catch (InterruptedException e) {
            throw new UndeclaredThrowableException(e);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void purge(String choice) throws IOException {
        LogAgent.LOGGER.fine("PURGE");

        Term term = new Term(DocumentIndexer.CONTENT, choice);
        writer.deleteDocuments(term);
        writer.commit();

        LogAgent.LOGGER.finer("documents " + writer.numDocs());
    }

    private int add(Path corpus, String choice)
        throws IOException,
               InterruptedException {
        LogAgent.LOGGER.fine("INDEX");

        List<Callable<String>> tasks = new ArrayList<Callable<String>>();
        try (DirectoryStream<Path> stream =
             Files.newDirectoryStream(corpus)) {
            for (Path file : stream) {
                DocumentPipeline pipeline = new DocumentPipeline(file);
                pipeline
                    .addJob(new DocumentJustifier(directory))
                    .addJob(new DocumentUpdater(choice))
                    .addJob(new DocumentIndexer(writer));

                tasks.add(pipeline);
            }
        }

        int completed = 0;
        for (Future<String> ft : executors.invokeAll(tasks)) {
            try {
                String docno = ft.get();
                completed++;
                LogAgent.LOGGER.finer("completed " + docno);
            }
            catch (ExecutionException e) {
                Throwable reason = e.getCause();
                if (!(reason instanceof AssemblerException)) {
                    throw new IllegalThreadStateException(reason.toString());
                }
                // LogAgent.LOGGER.info(message);
            }
        }

        writer.commit();

        return completed;
    }

    public TopDocs query(int count) {
        LogAgent.LOGGER.fine("QUERY");

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            return searcher.search(query, count);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void check() {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            for (int i = 0; i < reader.maxDoc(); i++) {
                Document doc = reader.document(i);
                LogAgent.LOGGER.info(DocumentIndexer.DOCNO + " " +
                                     doc.get(DocumentIndexer.DOCNO));
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static void main(String[] args) {
        LogAgent.setLevel(Level.FINE);

        /*
         *
         */
        Path query = Paths.get(args[0]);
        Path corpus = Paths.get(args[1]);
        Path index = Paths.get(args[2]);
        Path relevance = Paths.get(args[3]);
        Path output = Paths.get(args[4]);
        int count = Integer.parseInt(args[5]);
        int workers = Integer.parseInt(args[6]);

        /*
         *
         */
        int procs = Runtime.getRuntime().availableProcessors();
        if (workers > procs) {
            workers = procs;
        }

        try (InteractiveRetriever interaction =
             new InteractiveRetriever(query, index, workers);
             OutputStream outputStream = Files.newOutputStream(output);
             PrintStream printer = new PrintStream(outputStream, true)) {
            int round = 1;
            SelectionStrategy selector = new SequentialSelector(corpus);

            for (String choice : selector) {
                int indexed = interaction.index(corpus, choice);
                TopDocs hits = interaction.query(count);

                Timestamp now = new Timestamp(System.currentTimeMillis());
                StringJoiner result = new StringJoiner(",");
                result
                    .add(now.toString())
                    .add(String.valueOf(round))
                    .add(String.valueOf(indexed))
                    .add(choice)
                    .add(String.valueOf(hits.totalHits));
                printer.println(result);

                // int i = 1;
                // for (ScoreDoc hit : hits.scoreDocs) {
                //     Document doc = searcher.doc(hit.doc);
                //     StringJoiner result = new StringJoiner(" ");
                //     result
                //         .add(String.valueOf(i))
                //         .add(doc.get("docno"))
                //         .add(String.valueOf(hit.score));
                //     System.out.println(result.toString());
                //     i += 1;
                // }

                round++;
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
