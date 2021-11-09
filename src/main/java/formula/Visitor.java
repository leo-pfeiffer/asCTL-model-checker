package formula;

import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.Not;
import model.State;

import java.util.Set;

// visitor pattern
// https://www.infoworld.com/article/2077602/java-tip-98--reflect-on-the-visitor-design-pattern.html

public interface Visitor {
    public Set<State> visit(Visitable visitable, Set<State> states);
    public Set<State> visitBoolProp(BoolProp formula, Set<State> states);
    public Set<State> visitAtomicProp(AtomicProp formula, Set<State> states);
    public Set<State> visitAnd(And formula, Set<State> states);
    public Set<State> visitNot(Not formula, Set<State> states);
    // todo Not
    // todo ThereExistsNext
    // todo thereExistsUntil
    // todo thereExistsAlways
}