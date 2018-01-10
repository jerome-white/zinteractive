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
import org.apache.lucene.index.Term;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
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
        try (IndexReader reader = DirectoryReader.open(directory)) {
                IndexSearcher searcher = new IndexSearcher(reader);
		Term term = new Term(DocumentIndexer.DOCNO, terms.getName());
                Query query = new TermQuery(term);

                switch (searcher.count(query)) {
                case 0:
                    LogAgent.LOGGER.finer("+justified " + terms.getName());
		    break;
                case 1:
                    LogAgent.LOGGER.finer("-justified " + terms.getName());
                    throw new AssemblerException(terms.getName());
                default:
                    throw new IllegalStateException();
                }
            }
        catch (IOException ex) {
            throw new UndeclaredThrowableException(ex);
        }

	return terms;
    }
}
