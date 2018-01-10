package task;

import java.lang.RuntimeException;

public class AssemblerException extends RuntimeException {
    public AssemblerException(String docno) {
        super(docno);
    }
}
