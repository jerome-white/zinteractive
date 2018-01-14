package query;

import java.nio.file.Path;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BoostQuery;

import task.DocumentIndexer;
import util.window.StringWindow;
import util.window.ComprehensiveStringWindow;

import util.TermCollection;

public class NGramQuery extends WeightedQuery {
    int min_ngram;

    public NGramQuery(String query, int min_ngram) {
        super(query);

        this.min_ngram = min_ngram;
    }

    public NGramQuery(String query) {
        this(query, 4);
    }

    protected void addSubQuery(BooleanQuery.Builder builder, String item) {
        StringWindow window = new ComprehensiveStringWindow(item, min_ngram);
        for (String win : window) {
            builder.add(generate(item, win), Occur.SHOULD);
        }
    }
}
