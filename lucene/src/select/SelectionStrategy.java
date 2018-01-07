package select;

import report.Feedback;

public interface SelectionStrategy extends Iterable<String> {
    public void setFeedback(Feedback feedback);
}
