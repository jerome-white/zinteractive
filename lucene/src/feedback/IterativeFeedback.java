package feedback;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;

import util.RetrievalResult;

public class IterativeFeedback implements SystemFeedback {
    private List<String> ranking;
    private Map<String, Integer> delta;

    public IterativeFeedback() {
        ranking = new ArrayList<>();
        delta = new HashMap<>();
    }

    public void addRankings(Iterable<RetrievalResult> results) {
        List<String> updatedRanking = new ArrayList<>();

        delta.clear();

        for (RetrievalResult res : results) {
            String doc = res.getDocument();

            int previous = ranking.indexOf(doc);
            if (previous >= 0) {
                delta.put(doc, previous - updatedRanking.size());
            }

            updatedRanking.add(doc);
        }

        ranking = updatedRanking;
    }

    public Iterable<String> getRankings() {
        return ranking;
    }

    public Map<String, Integer> getDeltas() {
        return delta;
    }
}
