package select;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import util.Term;
import util.TermCollection;

public class SequentialSelector extends SelectionTemplate {
    LinkedList<Path> files;
    Iterator<Term> ptr;

    public SequentialSelector(Path corpus) {
        files = new LinkedList<Path>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(corpus)) {
            for (Path file : stream) {
                files.push(file);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        files.sort((o1, o2) -> o1.compareTo(o2));
        advance();
    }

    private void advance() {
        try {
            TermCollection termCollection = new TermCollection(files.pop());
            ptr = termCollection.iterator();
        }
        catch (NoSuchElementException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public boolean hasNext() {
        if (!ptr.hasNext()) {
            try {
                advance();
            }
            catch (IllegalStateException ex) {
                return false;
            }
        }

        return true;
    }

    public Collection<String> next() {
        Term term = ptr.next();

        return Arrays.asList(term.toString());
    }
}
