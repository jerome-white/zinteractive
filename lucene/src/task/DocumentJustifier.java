package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import util.TermCollection;

/*
 * Identify documents that require indexing. This is either the
 * documents that were deleted, or all documents if nothing has ever
 * been indexed.
 */
public class DocumentJustifier implements Assembler {
    private Directory directory;

    public DocumentJustifier(Directory directory) {
        this.directory = directory;
    }

    public TermCollection assemble(TermCollection terms) {
        try {
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser qp = new QueryParser("docno", analyzer);
            Query qry = qp.parse(terms.getName());

            IndexReader reader = DirectoryReader.open(directory);
            IndexSearcher searcher = new IndexSearcher(reader);

            switch (searcher.count(qry)) {
            case 0:
                LogAgent.LOGGER.finer(terms.getName() + " missing");
                return terms;
            case 1:
                throw new AssemblerException(terms.getName());
            default:
                throw new IllegalStateException();
            }
        }
        catch (ParseException | IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }

    }
}
