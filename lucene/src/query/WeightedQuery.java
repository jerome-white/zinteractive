package query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BoostQuery;

import task.DocumentIndexer;

public class WeightedQuery extends QueryHandler {
    public WeightedQuery(String query) {
        super(query);
    }

    protected Query generate(String item, String reference) {
        Term term = new Term(DocumentIndexer.CONTENT, item);
        TermQuery termQuery = new TermQuery(term);

        float boost = item.length() / reference.length();

        return new BoostQuery(termQuery, boost);
    }

    protected void addSubQuery(BooleanQuery.Builder builder, String item) {}

    public Query toQuery() {
        BooleanQuery.Builder bq = new BooleanQuery.Builder();

        for (String item : toString().split(" ")) {
            bq.add(generate(item, item), Occur.SHOULD);
            addSubQuery(bq, item);
        }

        return bq.build();
    }
}
