package task;

import util.TermCollection;

public interface Assembler {
    public TermCollection assemble(TermCollection terms)
        throws AssemblerException;
}
