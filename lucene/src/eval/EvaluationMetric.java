package eval;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collections;

import org.apache.lucene.search.ScoreDoc;

import util.RetrievalResult;

public abstract class EvaluationMetric {
    protected final Map<String, Integer> judgements;

    public EvaluationMetric(Path qrels, int topic) {
        judgements = new HashMap<String, Integer>();

        try (BufferedReader reader = Files.newBufferedReader(qrels)) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                String[] parts = line.trim().split("\\s+");
                if (Integer.valueOf(parts[0]) == topic) {
                    judgements.put(parts[2], Integer.valueOf(parts[3]));
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        int mostRelevant = Collections.max(judgements.values());
        if (!isRelevant(mostRelevant)) {
            throw new IllegalArgumentException(qrels.toString());
        }
    }

    private boolean isRelevant(int judgement) {
        return judgement > 0;
    }

    public boolean isRelevant(String document) {
        return isRelevant(getScore(document));
    }

    public int getScore(String document) {
        return judgements.getOrDefault(document, 0);
    }

    public double evaluate(List<RetrievalResult> results) {
        if (results.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return doEvaluation(results);
    }

    protected abstract double doEvaluation(List<RetrievalResult> results);
}
