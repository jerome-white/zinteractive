package query;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.search.Query;

import util.TermCollection;

public abstract class QueryHandler {
    String query;

    public QueryHandler(String query) {
        this.query = query;
    }

    public final String toString() {
        return query;
    }

    public abstract Query toQuery();
}
