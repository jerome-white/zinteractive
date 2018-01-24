package select;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

import feedback.SystemFeedback;

public abstract class SelectionTemplate implements SelectionStrategy,
                                                   Iterator<String> {
    private SystemFeedback feedback;
    private Set<String> guessed = new HashSet<String>();

    public abstract String getNext();

    public void setFeedback(SystemFeedback feedback) {
        this.feedback = feedback;
    }

    public Iterator<String> iterator() {
        return this;
    }

    public boolean hasNext() {
        return true;
    }

    public final String next() {
        while (hasNext()) {
            String choice = getNext();
            if (guessed.add(choice)) {
                return choice;
            }
        }

        throw new NoSuchElementException();
    }
}
