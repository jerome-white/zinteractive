package eval;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Iterator;

import util.RetrievalResult;

public class NormalizedDCG extends EvaluationMetric {
    List<Integer> relevantScores;

    public NormalizedDCG(Path qrels, int topic) {
        super(qrels, topic);

        relevantScores = new LinkedList<Integer>();
        for (Entry<String, Integer> entry : judgements.entrySet()) {
            if (isRelevant(entry.getKey())) {
                relevantScores.add(entry.getValue());
            }
        }
        relevantScores.sort(Collections.reverseOrder());
    }

    private double gain(int score) {
        return Math.pow(2, score) - 1;
    }

    public double evaluate(List<RetrievalResult> results) {
        int score;
        double dcg = 0;
        double idcg = 0;

        Iterator<Integer> authority = relevantScores.iterator();
        Iterator<RetrievalResult> estimates = results.iterator();

        for (int i = 1; estimates.hasNext(); i++) {
            double reduction = Math.log(1 + i);

            score = getScore(estimates.next().getDocument());
            dcg += gain(score) / reduction;

            if (authority.hasNext()) {
                score = authority.next();
                idcg += gain(score) / reduction;
            }
        }

        return dcg / idcg;
    }
}
