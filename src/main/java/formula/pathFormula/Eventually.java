package formula.pathFormula;

import formula.ENFConverter;
import formula.FormulaParser;
import formula.stateFormula.*;
import java.util.*;

public class Eventually extends PathFormula {
    public final StateFormula stateFormula;
    private Set<String> leftActions;
    private Set<String> rightActions;

    public Eventually(StateFormula stateFormula, Set<String> leftActions, Set<String> rightActions) {
        super();
        this.stateFormula = stateFormula;
        this.leftActions = leftActions;
        this.rightActions = rightActions;
    }

    public Set<String> getLeftActions() {
        return leftActions;
    }

    public Set<String> getRightActions() {
        return rightActions;
    }

    @Override
    public void writeToBuffer(StringBuilder buffer) {
        buffer.append(FormulaParser.EVENTUALLY_TOKEN);
        stateFormula.writeToBuffer(buffer);
    }

    @Override
    public StateFormula convertForAll(ENFConverter converter) {
        return converter.convertForAllEventually(this);
    }

    @Override
    public StateFormula convertThereExists(ENFConverter converter) {
        return converter.convertThereExistsEventually(this);
    }


}
