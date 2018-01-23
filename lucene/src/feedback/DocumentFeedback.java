package feedback;

import java.util.Deque;
import java.util.HashSet;
import java.util.SortedSet;
import java.util.LinkedList;

import util.RetrievalResult;

public class DocumentFeedback implements SystemFeedback {
    private int memory;
    private Deque<SortedSet<RetrievalResult>> history;

    public DocumentFeedback(int memory) {
	this.memory = memory;
	history = new LinkedList<>();
    }

    public DocumentFeedback() {
	this(Integer.MAX_INT);
    }
    
    public void add(Iterable<RetrievalResult> documents) {
	SortedSet<RetrievalResult> ranking = new HashSet<>();
	
	int i = 0;
	for (RetrievalResult result : documents) {
	    ranking.add(new RetrievalResult(result.getDocument(), i));
	    i--;
	}

	history.add(ranking);
	if (history.size() > memory) {
	    history.pop();
	}
    }

    public Iterable<RetrievalResult> latestResults() {
	return history.peekLast();
    }
    
    public Iterable<RetrievalResult> deltaRankings() {
	SortedSet<RetrievalResult> ranking = new HashSet<>();

	Iterator<SortedSet<RetrievalResult>> history.descendingIterator();
	try {
	    SortedSet<RetrievalResult> current = history.next();
	    SortedSet<RetrievalResult> previous = history.next();
	}
	catch (NoSuchElementException e) {
	    throw new IllegalStateException();
	}

	for (RetrievalResult source : previous) {
	    RetrievalResult target = current.get(source);
	    int delta = target.difference(source);

	    ranking.add(new RetrievalResult(source.getDocument(), delta));
	}

	return ranking;							 
    }
}
