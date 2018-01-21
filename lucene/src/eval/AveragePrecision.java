package eval;

import java.nio.file.Path;
import java.util.List;

import util.RetrievalResult;

public class AveragePrecision extends EvaluationMetric {
    private int relevant;

    public AveragePrecision(Path qrels, int topic) {
        super(qrels, topic);

        relevant = 0;
        for (String document : judgements.keySet()) {
            if (isRelevant(document)) {
                relevant++;
            }
        }
    }

    protected double doEvaluation(List<RetrievalResult> results) {
        int i = 1;
        int found = 0;
        double ap = 0;
        double previous_recall = 0;

        for (RetrievalResult hit : results) {
            if (isRelevant(hit.getDocument())) {
                found++;
            }

            double precision = found / i;
            double recall = found / relevant;

            ap += precision * (recall - previous_recall);

            previous_recall = recall;
            i++;
        }

        return ap;
    }
}
