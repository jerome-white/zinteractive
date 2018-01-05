package select;

import java.util.Collection;

import report.Feedback;

public interface SelectionStrategy extends Iterable<Collection<String>> {
    public void setFeedback(Feedback feedback);
}
