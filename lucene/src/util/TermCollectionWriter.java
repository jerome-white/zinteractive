package util;

import java.lang.Iterable;
import java.util.Iterator;
import java.util.StringJoiner;

public class TermCollectionWriter implements Iterator<String>,
                                             Iterable<String> {
    private StringJoiner header;
    private Iterator<Term> itr;

    public TermCollectionWriter(Iterable<Term> collection) {
        itr = collection.iterator();
        header = new StringJoiner(",");
        header
            .add("name")
            .add("ngram")
            .add("position");
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return itr.hasNext();
    }

    public String next() {
        String nxt;

        if (header != null) {
            nxt = header.toString();
            header = null;
        }
        else {
            nxt = itr.next().toCSV();
        }

        return nxt;
    }
}
