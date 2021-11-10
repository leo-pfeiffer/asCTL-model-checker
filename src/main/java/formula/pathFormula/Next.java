package formula.pathFormula;

import formula.FormulaParser;
import formula.Visitable;
import formula.Visitor;
import formula.stateFormula.*;
import model.State;

import java.util.Set;

public class Next extends PathFormula implements Visitable {
    public final StateFormula stateFormula;
    private Set<String> actions;

    public Next(StateFormula stateFormula, Set<String> actions) {
        this.stateFormula = stateFormula;
        this.actions = actions;
    }

    public Set<String> getActions() {
        return actions;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append(FormulaParser.NEXT_TOKEN);
        stateFormula.writeToBuffer(buffer);
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitNext(this, states);
    }
}
