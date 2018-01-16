package util;

public class RetrievalResult implements Comparable<RetrievalResult> {
    private final String document;
    private final double score;

    public RetrievalResult(String document, double score) {
        this.document = document;
        this.score = score;
    }

    public int compareTo(RetrievalResult o) {
        return Double.compare(o.getScore(), score); // descending!
    }

    public String getDocument() {
        return document;
    }

    public double getScore() {
        return score;
    }
}
