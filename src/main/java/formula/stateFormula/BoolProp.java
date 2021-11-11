package formula.stateFormula;

import formula.ENFConverter;
import formula.Visitable;
import formula.Visitor;
import model.State;

import java.util.Set;

public class BoolProp extends StateFormula implements Visitable {
    public final boolean value;

    public BoolProp(boolean value) {
        this.value = value;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        String stringValue = (value) ? "True" : "False";
        buffer.append(" " + stringValue + " ");
    }

    @Override
    public StateFormula convertToENF(ENFConverter converter) {
        return converter.convertBoolProp(this);
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitBoolProp(this, states);
    }
}
