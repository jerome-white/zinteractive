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
import util.LogAgent;
import util.TermCollection;
import util.TermCollectionWriter;

/*
 * Decrypt and re-write documents (to disk) that require updating.
 */
public class DocumentUpdater implements Assembler {
    private Collection<String> alterations;

    public DocumentUpdater(Collection<String> alterations) {
        this.alterations = alterations;
    }

    public DocumentUpdater(String alteration) {
        this(Arrays.asList(alteration));
    }

    public DocumentUpdater() {
        this(new ArrayList<String>());
    }

    public TermCollection assemble(TermCollection terms) {
        TermCollection ptr = terms;
        TermCollection scratch = new TermCollection();

	int changes = 0;
        for (String alt : alterations) {
            for (Term term : ptr) {
		if (term.is(alt)) {
		    scratch.add(term.decrypt());
		    changes++;
		}
		else {
		    scratch.add(term);
		}
                // Term t = term.is(alt) ? term.decrypt() : term;
                // scratch.add(t);
            }
            ptr = scratch;
            scratch = new TermCollection();
        }
	LogAgent.LOGGER.finer("decrypt " + terms.getName() + " " + changes);

        scratch.setLocation(terms.getLocation());
        TermCollectionWriter writer = new TermCollectionWriter(scratch);
        try {
            Files.write(scratch.getLocation(), writer);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return scratch;
    }
}
