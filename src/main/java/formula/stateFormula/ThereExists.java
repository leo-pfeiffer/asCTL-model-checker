package formula.stateFormula;

import formula.ENFConverter;
import formula.FormulaParser;
import formula.Visitable;
import formula.Visitor;
import formula.pathFormula.PathFormula;
import model.State;

import java.util.Set;

public class ThereExists extends StateFormula implements Visitable {
    public final PathFormula pathFormula;

    public ThereExists(PathFormula pathFormula) {
        this.pathFormula = pathFormula;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append("(");
        buffer.append(FormulaParser.THEREEXISTS_TOKEN);
        pathFormula.writeToBuffer(buffer);
        buffer.append(")");
    }

    @Override
    public StateFormula convertToENF(ENFConverter converter) {
        return converter.convertThereExists(this);
    }

    @Override
    public Set<State> accept(Visitor visitor, Set<State> states) {
        return visitor.visitThereExists(this, states);
    }
}
