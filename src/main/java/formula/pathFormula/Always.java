package formula.pathFormula;

import formula.FormulaParser;
import formula.Visitable;
import formula.Visitor;
import formula.stateFormula.*;
import model.State;

import java.util.*;

public class Always extends PathFormula implements Visitable {
    public final StateFormula stateFormula;
    private Set<String> actions = new HashSet<String>();

    public Always(StateFormula stateFormula, Set<String> actions) {
        this.stateFormula = stateFormula;
        this.actions = actions;
    }

    public Set<String> getActions() {
        return actions;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append(FormulaParser.ALWAYS_TOKEn);
        stateFormula.writeToBuffer(buffer);
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitAlways(this, states);
    }
}
