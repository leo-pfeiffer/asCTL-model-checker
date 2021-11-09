package formula.stateFormula;

import formula.Visitable;
import formula.Visitor;
import model.State;

import java.util.Set;

public class AtomicProp extends StateFormula implements Visitable {
    public final String label;

    public AtomicProp(String label) {
        this.label = label;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append(" " + label + " ");
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitAtomicProp(this, states);
    }
}
