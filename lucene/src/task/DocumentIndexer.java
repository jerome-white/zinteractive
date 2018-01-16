package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.StringField;

import util.LogAgent;
import util.TermCollection;

/*
 * Write files to the index.
 */
public class DocumentIndexer implements Callable<Void> {
    public static final String DOCNO = "docno";
    public static final String CONTENT = "content";

    private Path document;
    private IndexWriter index;

    public DocumentIndexer(Path document, IndexWriter index) {
        this.document = document;
        this.index = index;
    }

    public Void call() {
        Document doc = new Document();
        TermCollection terms = new TermCollection(document);
        String docno = terms.getName();
        Term term = new Term(DOCNO, docno);

        LogAgent.LOGGER.finer("index " + docno);

        doc.add(new StringField(DOCNO, docno, Field.Store.YES));
        doc.add(new TextField(CONTENT, terms.toString(), Field.Store.NO));
        try {
            index.updateDocument(term, doc);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return null;
    }
}
