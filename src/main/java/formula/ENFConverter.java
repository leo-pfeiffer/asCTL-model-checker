package formula;

import formula.pathFormula.Always;
import formula.pathFormula.Eventually;
import formula.pathFormula.Next;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.ForAll;
import formula.stateFormula.Not;
import formula.stateFormula.Or;
import formula.stateFormula.StateFormula;
import formula.stateFormula.ThereExists;

import java.util.HashSet;
import java.util.Set;

/**
 * Convert a state formula to existential normal form (ENF) for CTL:
 * For a in AP, the set of state formulae in ENF is given by
 * phi ::= true | a | phi1 & phi2 | !phi | EX phi | E (phi1 U phi2) | EG phi
 * See: Baier and Katoen (2008). Principles of Model Checking. (p. 332)
 *
 * This is extended to asCTL as explained in the report
 * */
public class ENFConverter {

    /**
     * Convert a formula to ENF.
     * */
    public StateFormula convertToENF(StateFormula formula) {
        // visitor pattern:
        // this method calls the convertToENF method of the formula, which in turn calls the appropriate ENF
        // conversion method in this ENFConverter class.
        return formula.convertToENF(this);
    }

    /**
     * Convert a And state formula to ENF.
     * @param formula the And state formula to convert
     * @return ENF of the state formula
     */
    public And convertAnd(And formula) {
        return new And(convertToENF(formula.left), convertToENF(formula.right));
    }

    /**
     * Convert an AtomicProp state formula to ENF.
     * @param formula the AtomicProp to convert
     * @return ENF of the atomic proposition
     */
    public AtomicProp convertAtomicProp(AtomicProp formula) {
        return formula;
    }

    /**
     * Convert a BoolProp state formula to ENF.
     * true -> true
     * false -> Not (true)
     * @param formula the BoolProp to convert
     * @return ENF of the BoolProp
     */
    public StateFormula convertBoolProp(BoolProp formula) {
        // if the boolean proposition is true, return the property
        if (formula.value) {
            return formula;
        }
        // else return the negation of true (Not (true))
        else {
            return new Not(new BoolProp(true));
        }
    }

    /**
     * Convert a ForAll state formula to ENF.
     * @param formula the ForAll state formula to convert
     * @return ENF of the ForAll state formula
     */
    public StateFormula convertForAll(ForAll formula) {
        // forAll must be followed by path formula.
        // Delegate the conversion to the appropriate method for the path formula
        return formula.pathFormula.convertForAll(this);
    }

    /**
     * Convert a ThereExists state formula to ENF.
     * @param formula the ThereExists state formula to convert
     * @return ENF of the ThereExists state formula
     */
    public StateFormula convertThereExists(ThereExists formula) {
        return formula.pathFormula.convertThereExists(this);
    }

    /**
     * Convert an AX formula to ENF.
     * AX phi = !E (X !enf(phi))
     * ForAll(Next(P)) = not(Exists(Next(not(ENF(P)))))
     * @param formula the AG formula to convert
     * @return ENF of the AG formula
     */
    public Not convertForAllNext(Next formula) {
        Next next = new Next(new Not(convertToENF(formula.stateFormula)), formula.getActions());
        return new Not(new ThereExists(next));
    }

    /**
     * Convert an EX formula to ENF.
     * EX phi = E X (enf(phi))
     * @param formula the EX state formula to convert
     * @return ENF of the EX state formula
     */
    public ThereExists convertThereExistsNext(Next formula) {
        Next next = new Next(convertToENF(formula.stateFormula), formula.getActions());
        return new ThereExists(next);
    }

    /**
     * Convert an AG formula to ENF.
     * AG phi = !E (true U enf(phi))
     * @param formula the AG formula to convert
     * @return ENF of the AG formula
     */
    public Not convertForAllAlways(Always formula) {
        // AG phi = !EF !phi = !E (true U phi)
        Set<String> preActions = new HashSet<>();
        Set<String> postActions = formula.getActions();
        Until until = new Until(new BoolProp(true), convertToENF(formula.stateFormula), preActions, postActions);
        return new Not(new ThereExists(until));
    }

    /**
     * Convert a EG formula to ENF.
     * EG phi = EG enf(phi)
     * @param formula the EG formula to convert
     * @return ENF of the EG formula
     */
    public ThereExists convertThereExistsAlways(Always formula) {
        return new ThereExists(new Always(convertToENF(formula.stateFormula), formula.getActions()));
    }

    /**
     * Convert an AU formula to ENF.
     * A (phi1 U phi2) =
     *      (!E ( !enf(phi2) U ( !enf(phi1) & !enf(phi2) )))
     *      &
     *      (!E G ( !enf(phi2) ))
     * @param formula the AU formula to convert
     * @return ENF of the AU formula
     */
    public And convertForAllUntil(Until formula) {

        StateFormula left = convertToENF(formula.left);
        StateFormula right = convertToENF(formula.right);

        // left side: ( !enf(phi2) U ( !enf(phi1) & !enf(phi2) ))
        Not leftSideUntil = new Not(right);
        And rightSideUntil = new And(new Not(left), new Not(right));
        Until until = new Until(leftSideUntil, rightSideUntil, formula.getLeftActions(), formula.getRightActions());

        // right side : G ( !enf(phi2) )
        Always always = new Always(new Not(right), formula.getRightActions());

        // combine the two sides
        return new And(new Not(new ThereExists(until)), new Not(new ThereExists(always)));
    }

    /**
     * Convert an EU formula to ENF.
     * E (phi1 U phi2) = E (enf(phi1) U enf(phi2))
     * @param formula the EU formula to convert
     * @return ENF of the EU formula
     * */
    public ThereExists convertThereExistsUntil(Until formula) {
        StateFormula left = convertToENF(formula.left);
        StateFormula right = convertToENF(formula.right);
        return new ThereExists(new Until(left, right, formula.getLeftActions(), formula.getRightActions()));
    }

    /**
     * Convert an AF formula to ENF.
     * AF phi = !E (G !enf(phi))
     * @param formula the AF formula to convert
     * @return ENF of the AF formula
     * */
    public Not convertForAllEventually(Eventually formula) {
        Always inner = new Always(new Not(convertToENF(formula.stateFormula)), formula.getRightActions());
        return new Not(new ThereExists(inner));
    }

    /**
     * Convert an EF formula to ENF.
     * EF phi = E (true U enf(phi))
     * @param formula the EF formula to convert
     * @return ENF of the EF formula
     * */
    public ThereExists convertThereExistsEventually(Eventually formula) {
        Until inner = new Until(new BoolProp(true), convertToENF(formula.stateFormula), formula.getLeftActions(), formula.getRightActions());
        return new ThereExists(inner);
    }

    /**
     * Convert a Not state formula to ENF.
     * @param formula the Not state formula to convert
     * @return ENF of the Not state formula
     * */
    public Not convertNot(Not formula) {
        return new Not(convertToENF(formula.stateFormula));
    }

    /**
     * Convert a Or state formula to ENF.
     * A || B -> ! (!A && !B)
     * @param formula the Or state formula to convert
     * @return ENF of the Or state formula
     */
    public Not convertOr(Or formula) {
        return new Not(new And(new Not(formula.left), new Not(formula.right)));
    }
}
