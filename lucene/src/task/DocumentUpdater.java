package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.concurrent.Callable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.UndeclaredThrowableException;

import util.Term;
import util.TermCollection;
import util.TermCollectionWriter;

public class DocumentUpdater implements Callable<TermCollection> {
    private TermCollection document;
    private Collection<String> alterations;

    public DocumentUpdater(TermCollection document,
                           Collection<String> alterations) {
        this.document = document;
        this.alterations = alterations;
    }

    public DocumentUpdater(TermCollection document,
                           String alteration) {
        this(document, Arrays.asList(alteration));
    }

    public DocumentUpdater(TermCollection document) {
        this(document, new ArrayList<String>());
    }

    public TermCollection call() {
        TermCollection ptr = document;
        TermCollection scratch = new TermCollection();

        for (String alt : alterations) {
            for (Term term : ptr) {
                Term t = term.is(alt) ? term.decrypt() : term;
                scratch.add(t);
            }
            ptr = scratch;
            scratch = new TermCollection();
        }
        scratch.setLocation(document.getLocation());

        TermCollectionWriter writer = new TermCollectionWriter(scratch);
        try {
            Files.write(scratch.getLocation(), writer);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return document;
    }
}
