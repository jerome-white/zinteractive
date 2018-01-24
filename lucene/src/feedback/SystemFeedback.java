package feedback;

import java.lang.Iterable;

import util.RetrievalResult;

public interface SystemFeedback {
    public void add(Iterable<RetrievalResult> documents);
    public Iterable<RetrievalResult> latestResults();
    public Iterable<RetrievalResult> deltaRankings();
}
