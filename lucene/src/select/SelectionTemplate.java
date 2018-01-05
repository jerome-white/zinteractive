package select;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

import report.Feedback;
import report.NoFeedback;

public abstract class SelectionTemplate
    implements SelectionStrategy,
               Iterator<Collection<String>> {
    private Feedback feedback = new NoFeedback();

    public boolean hasNext() {
        return true;
    }

    public void setFeedback(Feedback feedback) {
        this.feedback = feedback;
    }

    public Iterator<Collection<String>> iterator() {
        return this;
    }
}
