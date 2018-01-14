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

public class WeightedQuery extends QueryBuilder {
    int minimum_ngram;

    public WeightedQuery(Path query, int minimum_ngram) {
        super(query);

        this.minimum_ngram = minimum_ngram;
    }

    public WeightedQuery(Path query) {
        this(query, 4);
    }

    private Query generate(String item, String reference) {
        Term term = new Term(DocumentIndexer.CONTENT, item);
        TermQuery termQuery = new TermQuery(term);

        float boost = item.length() / reference.length();

        return new BoostQuery(termQuery, boost);
    }

    public Query toQuery() {
        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String item : toString().split(" ")) {
            bq.add(generate(item, item), Occur.SHOULD);
            StringWindow window = new ComprehensiveStringWindow(item, 4);
            for (String win : window) {
                bq.add(generate(item, win), Occur.SHOULD);
            }
        }

        return bq.build();
    }
}
