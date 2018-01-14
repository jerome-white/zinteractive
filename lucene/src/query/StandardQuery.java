package query;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.lucene.search.Query;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.TermCollection;
import task.DocumentIndexer;

public class StandardQuery extends QueryHandler {
    private Analyzer analyzer;

    public StandardQuery(String query, Analyzer analyzer) {
        super(query);
        this.analyzer = analyzer;
    }

    public StandardQuery(String query) {
        this(query, new WhitespaceAnalyzer());
    }

    public Query toQuery() {
        try {
            QueryParser qp = new QueryParser(DocumentIndexer.CONTENT,
                                             analyzer);
            return qp.parse(toString());
        }
        catch (ParseException e) {
            throw new UndeclaredThrowableException(e);
        }
    }
}
