package formula.stateFormula;

import formula.Visitable;
import formula.Visitor;
import model.State;

import java.util.Set;

public class And extends StateFormula implements Visitable {
    public final StateFormula left;
    public final StateFormula right;

    public And(StateFormula left, StateFormula right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append("(");
        left.writeToBuffer(buffer);
        buffer.append(" && ");
        right.writeToBuffer(buffer);
        buffer.append(")");
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitAnd(this, states);
    }
}