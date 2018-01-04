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

    public TermCollection(Path document) {
        try (BufferedReader reader = Files.newBufferedReader(document)) {
            boolean headerRead = false;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                if (headerRead) {
                    String[] parts = line.split(",", 3);
                    int position = Integer.valueOf(parts[2]);
                    Term term = new Term(parts[0], parts[1], position);
                    add(term);
                }
                else {
                    headerRead = true;
                }
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        this.document = document;
    }

    public String toString() {
        StringJoiner sentence = new StringJoiner(" ");

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

    public int decrypt(String value) {
        int altered = 0;

        for (Term term : this) {
            if (!term.isEncrypted() && value.equals(term.toString())) {
                term.decrypt();
                altered++;
            }
        }

        return altered;
    }
}
