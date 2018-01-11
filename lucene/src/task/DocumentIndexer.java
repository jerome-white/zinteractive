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

/*
 * Write files to the index.
 */
public class DocumentIndexer implements Assembler {
    public static final String DOCNO = "docno";
    public static final String CONTENT = "content";

    private IndexWriter index;

    public DocumentIndexer(IndexWriter index) {
        this.index = index;
    }

    public TermCollection assemble(TermCollection terms) {
        LogAgent.LOGGER.finer("index " + terms.getName());

        Document doc = new Document();

        doc.add(new StringField(DOCNO, terms.getName(), Field.Store.YES));
        doc.add(new TextField(CONTENT, terms.toString(), Field.Store.NO));
        try {
            index.addDocument(doc);
        }
        catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return terms;
    }
}
