package task;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;

import util.LogAgent;
import util.TermCollection;

public class DocumentPipeline implements Callable<String> {
    private Path path;
    private List<Assembler> jobs;

    public DocumentPipeline(Path path) {
        this.path = path;
        jobs = new ArrayList<Assembler>();
    }

    public DocumentPipeline addJob(Assembler job) {
        jobs.add(job);

        return this;
    }

    public String call() {
        TermCollection document = new TermCollection(path);

        for (Assembler job : jobs) {
            document = job.assemble(document);
        }

        return document.getName();
    }
}
