package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import util.TermCollection;

public class DocumentIndexer implements Callable<String> {
    private Path document;
    private IndexWriter index;
    private Collection<String> alterations;

    public DocumentIndexer(Path document,
                           IndexWriter index,
                           Collection<String> alterations) {
        this.document = document;
        this.index = index;
        this.alterations = alterations;
    }

    public DocumentIndexer(Path document,
                           IndexWriter index,
                           String alteration) {
        this(document, index, Arrays.asList(alteration));
    }

    public DocumentIndexer(Path document, IndexWriter index) {
        this(document, index, new ArrayList<String>());
    }

    public String call() {
        TermCollection terms = new TermCollection(document);
        String name = terms.getName();

        int changed = 0;
        for (String alt : alterations) {
            changed += terms.decrypt(alt);
        }

        if (changed > 0 || alterations.isEmpty()) {
            Document doc = new Document();

            doc.add(new StringField(name, "docno", Field.Store.YES));
            doc.add(new TextField(terms.toString(), "text", Field.Store.NO));

            try {
                index.addDocument(doc);
            }
            catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

        return name;
    }
}
