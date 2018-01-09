package select;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.DirectoryStream;
import java.util.Deque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.NoSuchElementException;

import util.Term;
import util.TermCollection;
import util.LogAgent;

public class SequentialSelector extends SelectionTemplate {
    Deque<String> cache;
    Iterator<Term> ptr;
    LinkedList<Path> files;

    public SequentialSelector(Path corpus) {
        files = new LinkedList<Path>();
        cache = new ArrayDeque<String>();
        ptr = Collections.emptyIterator();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(corpus)) {
            for (Path file : stream) {
                files.push(file);
            }
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        files.sort((o1, o2) -> o1.compareTo(o2));

        load();
    }

    public void load() {
        while (!files.isEmpty()) {
            if (!ptr.hasNext()) {
                Path path = files.pop();
                TermCollection terms = new TermCollection(path);
                ptr = terms.iterator();
            }

            while (ptr.hasNext()) {
                Term term = ptr.next();
                if (term.isEncrypted()) {
                    cache.add(term.toString());
                    if (cache.size() > 1) {
                        return;
                    }
                }
            }
        }
    }

    public boolean hasNext() {
        return !cache.isEmpty();
    }

    public String getNext() {
        load();
        if (hasNext()) {
            return cache.pop();
        }

        throw new NoSuchElementException();
    }
}
