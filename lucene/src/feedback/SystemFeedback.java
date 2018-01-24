package feedback;

import java.util.Map;

import util.RetrievalResult;

public interface SystemFeedback {
    public void add(Iterable<RetrievalResult> results);
    public Iterable<String> getRankings();
    public Map<String, Integer> getDeltas();
}
