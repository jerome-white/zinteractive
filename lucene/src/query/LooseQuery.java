package query;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.nio.file.Files;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.DisjunctionMaxQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.Query;

import util.TermCollection;

public class LooseQuery extends QueryBuilder {
    public LooseQuery(Path query) {
        super(query);
    }

    public LooseQuery(TermCollection terms) {
        super(terms);
    }

    public Query toQuery() {
        Collection<Query> parts = new ArrayList<Query>();
        for (String s : toString().split(" ")) {
            parts.add(new FuzzyQuery(new Term("content", s)));
        }

        return new DisjunctionMaxQuery(parts, 0);
    }
}
