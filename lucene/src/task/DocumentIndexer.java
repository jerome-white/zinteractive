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

import util.LogAgent;
import util.TermCollection;

public class DocumentIndexer implements Callable<Void> {
    private TermCollection document;
    private IndexWriter index;
    private Collection<String> alterations;

    public DocumentIndexer(TermCollection document,
                           Collection<String> alterations,
                           IndexWriter index) {
        this.document = document;
        this.alterations = alterations;
        this.index = index;
    }

    public DocumentIndexer(TermCollection document,
                           String alteration,
                           IndexWriter index) {
        this(document, Arrays.asList(alteration), index);
    }

    public DocumentIndexer(TermCollection document, IndexWriter index) {
        this(document, new ArrayList<String>(), index);
    }

    public Void call() {
        Document doc = new Document();
        String name = document.getName();

        LogAgent.LOGGER.info(name + " index");

        doc.add(new StringField(name, "docno", Field.Store.YES));
        doc.add(new TextField(document.toString(), "content",
                              Field.Store.NO));

        try {
            index.addDocument(doc);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return null;
    }
}
