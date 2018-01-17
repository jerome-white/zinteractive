package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.lang.reflect.UndeclaredThrowableException;

import util.Term;
import util.LogAgent;
import util.TermCollection;
import util.TermCollectionWriter;

/*
 * Decrypt and re-write documents (to disk) that require updating.
 */
public class DocumentUpdater implements Callable<String> {
    private Path document;
    private Collection<String> alterations;

    public DocumentUpdater(Path document, Collection<String> alterations) {
        this.document = document;
        this.alterations = alterations;
    }

    public DocumentUpdater(Path document, String alteration) {
        this(document, Arrays.asList(alteration));
    }

    public String call() {
        int changes = 0;
        TermCollection terms = new TermCollection(document);

        LogAgent.LOGGER.finer("decrypt " + terms.getName());

        for (String alt : alterations) {
            terms = terms.decrypt(alt);
        }

        TermCollectionWriter writer = new TermCollectionWriter(terms);
        try {
            Files.write(document, writer);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return terms.getName();
    }
}
