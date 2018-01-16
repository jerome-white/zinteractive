package util;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.StringJoiner;

public class TrecResultsWriter implements Iterator<String>,
                                          Iterable<String> {
    private int rank;
    private String topic;
    private Iterator<RetrievalResult> itr;

    public TrecResultsWriter(Iterable<RetrievalResult> collection,
                             int topic) {
        this.topic = String.valueOf(topic);
        itr = collection.iterator();
        rank = 1;
    }

    public TrecResultsWriter(Iterable<RetrievalResult> collection) {
        this(collection, 0);
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return itr.hasNext();
    }

    public String next() {
        RetrievalResult result = itr.next();
        StringJoiner trec = new StringJoiner(" ");

        trec
            .add(topic) // qid
            .add("Q" + 0) // iter
            .add(result.getDocument()) // docno
            .add(String.valueOf(rank)) // rank
            .add(String.valueOf(result.getScore())) // sim(ilarity)
            .add("lucene"); // run_id

        rank++;

        return trec.toString();
    }
}
