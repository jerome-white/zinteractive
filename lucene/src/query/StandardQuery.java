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

public class StandardQuery extends QueryBuilder {
    private Analyzer analyzer;

    public StandardQuery(Path query, Analyzer analyzer) {
        super(query);
        this.analyzer = analyzer;
    }

    public StandardQuery(Path query) {
        this(query, new WhitespaceAnalyzer());
    }

    public StandardQuery(TermCollection terms, Analyzer analyzer) {
        super(terms);
        this.analyzer = analyzer;
    }

    public StandardQuery(TermCollection terms) {
        this(terms, new WhitespaceAnalyzer());
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
