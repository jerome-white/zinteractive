package util;

import java.lang.Comparable;

public class Term implements Comparable<Term> {
    private int position;

    private String name;
    private String ngram;

    public Term(String name, String ngram, int position) {
        this.name = name;
        this.ngram = ngram;
        this.position = position;
    }

    public int compareTo(Term o) {
        int cmp;

        // 1. compare position
        cmp = Integer.compare(position, o.position);
        if (cmp != 0) {
            return cmp;
        }

        // 2. compare length
        cmp = Integer.compare(length(), o.length());
        if (cmp != 0) {
            return cmp;
        }

        // 3. compare name
        return name.compareTo(o.name);
    }

    public int length() {
        return ngram.length();
    }

    public String toString() {
        return name;
    }

    public void decrypt() {
        name = ngram;
    }

    public boolean isEncrypted() {
        return !name.equals(ngram);
    }
}
