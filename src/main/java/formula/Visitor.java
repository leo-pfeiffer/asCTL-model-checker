package formula;

import formula.pathFormula.Always;
import formula.pathFormula.Next;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.Not;
import formula.stateFormula.ThereExists;
import model.State;

import java.util.Set;

// visitor pattern
// https://www.infoworld.com/article/2077602/java-tip-98--reflect-on-the-visitor-design-pattern.html

public interface Visitor {
    Set<State> visit(Visitable visitable, Set<State> states);
    Set<State> visitBoolProp(BoolProp formula, Set<State> states);
    Set<State> visitAtomicProp(AtomicProp formula, Set<State> states);
    Set<State> visitAnd(And formula, Set<State> states);
    Set<State> visitNot(Not formula, Set<State> states);
    Set<State> visitThereExists(ThereExists formula, Set<State> states);
    Set<State> visitNext(Next formula, Set<State> states);
    Set<State> visitUntil(Until formula, Set<State> states);
    Set<State> visitAlways(Always formula, Set<State> states);
}