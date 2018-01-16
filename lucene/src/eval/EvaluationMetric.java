package eval;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

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

                String[] parts = line.split(" ");
                if (Integer.valueOf(parts[0]) == topic) {
                    judgements.put(parts[2], Integer.valueOf(parts[3]));
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public boolean isRelevant(String document) {
        return getScore(document) > 0;
    }

    public int getScore(String document) {
        return judgements.getOrDefault(document, 0);
    }

    public abstract double evaluate(List<RetrievalResult> results);
}
