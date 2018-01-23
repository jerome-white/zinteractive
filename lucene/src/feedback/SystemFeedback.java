package feedback;

public interface SystemFeedback {
    public void add(Iterable<RetrievalResult> documents);
    public Iterable<RetrievalResult> latestResults();
    public Iterable<RetrievalResult> deltaRankings();
}
