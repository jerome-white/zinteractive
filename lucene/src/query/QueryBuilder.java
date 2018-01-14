package query;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.lucene.search.Query;

public abstract class QueryBuilder {
    String query;

    public QueryBuilder(Path query) {
        try {
            this.query = new String(Files.readAllBytes(query));
        }
        catch (IOException e) {
            throw new UndeclaredThrowableException(e);
        }
    }

    public final String toString() {
        return query;
    }

    public abstract Query toQuery();
}
