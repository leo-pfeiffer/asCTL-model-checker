package formula.stateFormula;

import formula.ENFConverter;
import formula.FormulaParser;
import formula.Visitable;
import formula.Visitor;
import model.State;

import java.util.Set;

public class Not extends StateFormula implements Visitable {
    public final StateFormula stateFormula;

    public Not(StateFormula stateFormula) {
        this.stateFormula = stateFormula;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append(FormulaParser.NOT_TOKEN);
        buffer.append("(");
        stateFormula.writeToBuffer(buffer);
        buffer.append(")");
    }

    @Override
    public StateFormula convertToENF(ENFConverter converter) {
        return converter.convertNot(this);
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitNot(this, states);
    }
}
