package feedback;

import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
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

    public boolean isLoaded() {
	return history.size() > 1;
    }
    
    public Iterable<RetrievalResult> deltaRankings() {
	if (!isLoaded()) {
	    throw new IllegalStateException();
	}
	
	SortedSet<RetrievalResult> ranking = new HashSet<>();

	Iterator<SortedSet<RetrievalResult>> itr=history.descendingIterator();
	SortedSet<RetrievalResult> ultimate = itr.next();
	SortedSet<RetrievalResult> penultimate = itr.next();

	for (RetrievalResult source : penultimate) {
	    RetrievalResult target = ultimate.get(source);
	    int delta = target.difference(source);

	    ranking.add(new RetrievalResult(source.getDocument(), delta));
	}

	return ranking;							 
    }
}
