package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.Callable;
import java.lang.reflect.UndeclaredThrowableException;

import org.apache.lucene.store.Directory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import util.TermCollection;

public class DocumentPipeline implements Callable<String> {
    private Path path;
    private Deque<Assembler> jobs;

    public DocumentPipeline(Path path) {
        this.path = path;
        jobs = new ArrayDeque<Assembler>();
    }

    public DocumentPipeline addJob(Assembler job) {
        jobs.add(job);

        return this;
    }

    public String call() {
        TermCollection document = new TermCollection(path);

        try {
            for (Assembler job : jobs) {
                document = job.assemble(document);
            }
        }
        catch (AssemblerException ae) {
            return null;
        }

        return document.getName();
    }
}
