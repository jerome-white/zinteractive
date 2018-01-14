package util;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.StringJoiner;
import java.util.TreeSet;

public class TermCollection extends TreeSet<Term> {
    private Path document;

    public TermCollection() {
        super();
    }

    public TermCollection(Path document, boolean hasHeader) {
        try (BufferedReader reader = Files.newBufferedReader(document)) {
            for (int i = 0; ; i++) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (i == 0 && hasHeader) {
                    continue;
                }

                String[] parts = line.split(",", 4);
                int position = Integer.valueOf(parts[2]);
                Term term = new Term(parts[0], parts[1], position);
                add(term);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        setLocation(document);
    }

    public TermCollection(Path document) {
        this(document, true);
    }

    public String toString() {
        StringJoiner sentence = new StringJoiner(" ");
        sentence.setEmptyValue("");

        for (Term term : this) {
            sentence.add(term.toString());
        }

        return sentence.toString();
    }

    public String getName() {
        return document.getFileName().toString();
    }

    public Path getLocation() {
        return document;
    }

    public void setLocation(Path path) {
        if (document != null) {
            throw new IllegalArgumentException();
        }
        document = path;
    }
}
