package task;

import java.nio.file.Path;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.Callable;

//import util.LogAgent;
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
