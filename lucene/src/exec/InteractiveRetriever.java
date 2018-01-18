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
import java.nio.file.attribute.FileTime;
import java.time.Instant;
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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import util.RetrievalResult;
import util.TrecResultsWriter;
import task.DocumentIndexer;
import task.DocumentUpdater;
import eval.EvaluationMetric;
import eval.NormalizedDCG;
import query.NGramQuery;
import query.QueryHandler;
import select.SelectionStrategy;
import select.SequentialSelector;

public class InteractiveRetriever implements AutoCloseable {
    private Path corpus;
    private IndexWriter writer;
    private ExecutorService executors;
    private Directory directory;
    private FileTime marker;

    public InteractiveRetriever(Path corpus, Path index, int workers) {
        LogAgent.LOGGER.fine("INITIALIZE: Lucene");

        Analyzer analyzer = new WhitespaceAnalyzer();
        try {
            directory = new NIOFSDirectory(index);

            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            writer = new IndexWriter(directory, config);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        this.corpus = corpus;
        executors = Executors.newFixedThreadPool(workers);
        marker = FileTime.from(Instant.MIN);
    }

    public void close() {
        executors.shutdown();
    }

    public void index() {
        LogAgent.LOGGER.fine("INDEX");

        List<Callable<Void>> tasks = new ArrayList<>();

        try (DirectoryStream<Path> ds = Files.newDirectoryStream(corpus)) {
            for (Path file : ds) {
                FileTime modified = Files.getLastModifiedTime(file);
                if (modified.compareTo(marker) > 0) {
                    tasks.add(new DocumentIndexer(file, writer));
                }
            }
            executors.invokeAll(tasks);
            writer.commit();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        catch (InterruptedException e) {
            throw new IllegalThreadStateException();
        }

        marker = FileTime.from(Instant.now());
    }

    public void update(String choice) {
        List<Callable<String>> tasks = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            Term term = new Term(DocumentIndexer.CONTENT, choice);
            Query query = new TermQuery(term);
            TopDocs docs = searcher.search(query, length());

            for (ScoreDoc hit : docs.scoreDocs) {
                Document doc = searcher.doc(hit.doc);
                Path target = corpus.resolve(doc.get("docno"));

                tasks.add(new DocumentUpdater(target, choice));
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        try {
            executors.invokeAll(tasks);
        }
        catch (InterruptedException e) {
            throw new IllegalThreadStateException();
        }
    }

    public List<RetrievalResult> query(Query query, int count) {
        LogAgent.LOGGER.fine("QUERY");

        List<RetrievalResult> aggregate = new ArrayList<>();

        try (IndexReader reader = DirectoryReader.open(directory)) {
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs results = searcher.search(query, count);

            for (ScoreDoc hit : results.scoreDocs) {
                String docno = reader
                    .document(hit.doc)
                    .get(DocumentIndexer.DOCNO);
                RetrievalResult rr = new RetrievalResult(docno, hit.score);

                aggregate.add(rr);
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return aggregate;
    }

    public int df(String choice) {
        try (IndexReader reader = DirectoryReader.open(directory)) {
            Term term = new Term(DocumentIndexer.CONTENT, choice);

            return reader.docFreq(term);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public int length() {
        return writer.numDocs();
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
        int topic = Integer.parseInt(args[5]);
        int count = Integer.parseInt(args[6]);
        int workers = Integer.parseInt(args[7]);

        /*
         *
         */
        int procs = Runtime.getRuntime().availableProcessors();
        if (workers > procs) {
            workers = procs;
        }

        try (InteractiveRetriever interaction =
             new InteractiveRetriever(corpus, index, workers)) {
            EvaluationMetric evaluator = new NormalizedDCG(relevance, topic);

            String queryString = new String(Files.readAllBytes(query));
            QueryHandler q = new NGramQuery(queryString);
            Query luceneQuery = q.toQuery();

            int round = 1;
            double metric;
            SelectionStrategy selector = new SequentialSelector(corpus);

            interaction.index();

            for (String choice : selector) {
                int frequency = interaction.df(choice);

                interaction.update(choice);
                interaction.index();
                List<RetrievalResult> hits = interaction.query(luceneQuery,
                                                               count);

                StringJoiner fname = new StringJoiner("-");
                fname
                    .add(String.valueOf(round))
                    .add(choice);
                Path destination = output.resolve(fname.toString());
                TrecResultsWriter writer = new TrecResultsWriter(hits);
                Files.write(destination, writer);

                try {
                    metric = evaluator.evaluate(hits);
                }
                catch (IllegalArgumentException e) {
                    LogAgent.LOGGER.warning("No results " + choice);
                    metric = 0;
                }

                Timestamp now = new Timestamp(System.currentTimeMillis());
                StringJoiner result = new StringJoiner(",");
                result
                    .add(now.toString())
                    .add(String.valueOf(topic))
                    .add(String.valueOf(round))
                    .add(choice)
                    .add(String.valueOf(frequency))
                    .add(String.valueOf(interaction.length()))
                    .add(String.valueOf(metric));
                LogAgent.LOGGER.info("REPORT " + result.toString());

                round++;
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
