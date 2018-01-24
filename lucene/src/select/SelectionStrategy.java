package select;

import feedback.SystemFeedback;

public interface SelectionStrategy extends Iterable<String> {
    public void setFeedback(SystemFeedback feedback);
}
