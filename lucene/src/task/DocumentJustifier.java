package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import util.TermCollection;

public class DocumentJustifier implements Callable<TermCollection> {
    private Path document;
    private Analyzer analyzer;
    private Directory directory;

    public DocumentJustifier(Path document,
                             Analyzer analyzer,
                             Directory directory) {
        this.document = document;
        this.analyzer = analyzer;
        this.directory = directory;
    }

    public TermCollection call() {
        TermCollection terms = new TermCollection(document);
        LogAgent.LOGGER.info(terms.getName());

        try {
            QueryParser qp = new QueryParser("docno", analyzer);
            Query qry = qp.parse(terms.getName());

            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            switch (searcher.count(qry)) {
            case 0:
                return terms;
            case 1:
                return null;
            default:
                throw new IllegalStateException();
            }
        }
        catch (ParseException | IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }

    }
}
