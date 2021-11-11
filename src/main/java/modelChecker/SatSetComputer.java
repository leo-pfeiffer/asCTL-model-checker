package modelChecker;

import formula.Visitable;
import formula.Visitor;
import formula.pathFormula.Always;
import formula.pathFormula.Next;
import formula.pathFormula.PathFormula;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.Not;
import formula.stateFormula.ThereExists;
import model.Model;
import model.State;

import formula.stateFormula.StateFormula;
import model.Transition;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static utils.SetOperations.*;

/**
 * Compute satisfaction set for a model.
 * */
public class SatSetComputer implements Visitor {

    private final Model model;

    public SatSetComputer(Model model) {
        this.model = model;
    }

    /**
     * Compute satisfaction set for a state formula.
     * Formula is assumed to be in ENF.
     * @param formula state formula
     * @return set of states satisfying the formula
     * */
    public Set<State> computeSatSet(StateFormula formula, Set<State> states) {
        assert (formula instanceof Visitable);
        return this.visit((Visitable) formula, states);
    }

    /**
     * Method for the visitor interface.
     * Delegates to the appropriate method for the formula.
     * */
    @Override
    public Set<State> visit(Visitable visitable, Set<State> states) {
        return visitable.accept(this, states);
    }

    /**
     * Sat Set for boolean proposition.
     * This is true in ENF.
     * SatSet = {S}
     * */
    @Override
    public Set<State> visitBoolProp(BoolProp formula, Set<State> states) {
        // in ENF, this is always true
        return states;
    }

    /**
     * Sat Set for atomic proposition.
     * SatSet = {s in S | a in L(s)}.
     * */
    @Override
    public Set<State> visitAtomicProp(AtomicProp formula, Set<State> states) {
        Set<State> filtered = new HashSet<>();

        // retain all states where the labels include the atomic proposition
        for (State s : states) {
            if (Arrays.asList(s.getLabel()).contains(formula.label)) {
                filtered.add(s);
            }
        }
        return filtered;
    }

    /**
     * Sat Set for conjunction.
     * SatSet = {Sat(left) and Sat(right)}
     * */
    @Override
    public Set<State> visitAnd(And formula, Set<State> states) {

        Set<State> satSetLeft = computeSatSet(formula.left, states);
        Set<State> satSetRight = computeSatSet(formula.right, states);

        // intersection of left and right
        return setIntersection(satSetLeft, satSetRight);
    }

    /**
     * Sat Set for negation.
     * SatSet = S w/o Sat(phi)
     * */
    @Override
    public Set<State> visitNot(Not formula, Set<State> states) {
        Set<State> satSet = computeSatSet(formula.stateFormula, states);
        return setDifference(states, satSet);
    }

    /**
     * Sat Set for There Exists.
     * */
    @Override
    public Set<State> visitThereExists(ThereExists formula, Set<State> states) {
        PathFormula pathFormula = formula.pathFormula;

        // must be one of the following
        assert (pathFormula instanceof Next || pathFormula instanceof Until || pathFormula instanceof Always);

        // delegate to the appropriate path formula visitor
        return this.visit((Visitable) pathFormula, states);
    }

    /**
     * Sat Set for Next.
     * SatSet = {s in S | Post(s) intersect Sat(formula) != {}}
     * */
    @Override
    public Set<State> visitNext(Next formula, Set<State> states) {

        // compute the sat set for the state formula first
        Set<State> satSet = computeSatSet(formula.stateFormula, states);

        // filter the sat set of the state formula by the pre-actions
        satSet = this.satSetPreActions(satSet, formula.getActions());

        // {s in S | Post(s) intersect Sat(formula) != {}}
        return this.postIntersectStatesNotEmpty(satSet);
    }

    /**
     * Sat Set for Until.
     *
     * ALGORITHM:
     * T := Sat(right)
     * while {s in Sat(left) w/o T | Post(s) intersect T != {}} != {} do:
     *      let s in {s in Sat(left) w/o T | Post(s) intersect T != {}};
     *      T := T union {s};
     * od;
     * SatSet = T;
     * */
    @Override
    public Set<State> visitUntil(Until formula, Set<State> states) {

        // todo not sure if action index is handled correctly

        // sat sets for left and right ignoring the action index
        Set<State> satSetLeft = computeSatSet(formula.left, states);
        Set<State> satSetRight = computeSatSet(formula.right, states);

        // consider action index
        satSetLeft = this.satSetPostActions(satSetLeft, formula.getLeftActions());
        satSetRight = this.satSetPreActions(satSetRight, formula.getRightActions());

        // T := Sat(right)
        Set<State> finalSatSet = new HashSet<>(satSetRight);

        // while {s in Sat(left) w/o T | Post(s) intersect T != {}} != {} do
        while (true) {

            // {s in Sat(left) w/o T}
            Set<State> satLeftPrime = setDifference(satSetLeft, finalSatSet);

            // {satLeftPrime | Post(s) intersect T != {}}
            Set<State> sub = this.postIntersectStatesNotEmpty(satLeftPrime);

            if (sub.isEmpty()) {
                break;
            }

            // T union {s}
            finalSatSet.addAll(sub);
        }

        return finalSatSet;
    }

    /**
     * Sat Set for Always.
     *
     * ALGORITHM:
     * T := Sat(phi)
     * while {s in T | Post(s) intersect T != {}} != {} do:
     *      let s in {s in T | Post(s) intersect T != {}};
     *      T := T w/o {s};
     * od;
     * SatSet = T;
     * */
    @Override
    public Set<State> visitAlways(Always formula, Set<State> states) {
        Set<State> satSet = computeSatSet(formula.stateFormula, states);

        // filter the sat set of the state formula by the pre-actions
        satSet = this.satSetPreActions(satSet, formula.getActions());

        Set<State> finalSatSet = new HashSet<>(satSet);

        while(true) {
            // {s in S}
            Set<State> sub = this.postIntersectStatesNotEmpty(finalSatSet);

            if (sub.isEmpty()) {
                break;
            }
            // T := T w/o {s}
            finalSatSet.removeAll(sub);
        }
        return finalSatSet;
    }

    /**
     * From a set of states, get the ones that satisfy the pre-actions.
     * @param states set of states
     * @param actions set of actions from the pre-actions
     * @return set of states that satisfy the pre-actions
     * */
    private Set<State> satSetPreActions(Set<State> states, Set<String> actions) {

        // todo test this although I think it should work

        // nothing to do if there are no pre-actions
        if (actions.isEmpty()) {
            return states;
        }

        Set<State> filteredStates = new HashSet<>();
        for (State state : states) {

            // get the incoming transitions into the current state
            Set<Transition> incomingTransitions = state.getIncomingTransitions(model);

            // retain only those transitions that are in the set of actions
            boolean retain = false;
            for (Transition transition : incomingTransitions) {

                Set<String> transitionActions = new HashSet<>(Arrays.asList(transition.getActions()));

                // if transition contains correct action, then we're done
                if (!Collections.disjoint(actions, transitionActions)) {
                    retain = true;
                    break;
                }
            }

            if (retain) {
                filteredStates.add(state);
            }
        }

        return filteredStates;
    }

    private Set<State> satSetPostActions(Set<State> states, Set<String> actions) {

        // todo test this

        // nothing to do if there are no post-actions
        if (actions.isEmpty()) {
            return states;
        }

        Set<State> filteredStates = new HashSet<>();

        for (State state : states) {

            // get the outgoing transitions from the current state
            Set<Transition> outgoingTransitions = state.getOutgoingTransitions(model);

            // retain only those transitions that are in the set of actions
            boolean retain = false;
            for (Transition transition : outgoingTransitions) {

                Set<String> transitionActions = new HashSet<>(Arrays.asList(transition.getActions()));

                // if transition contains correct action, then we're done
                if (!Collections.disjoint(actions, transitionActions)) {
                    retain = true;
                    break;
                }
            }
            if (retain) {
                filteredStates.add(state);
            }
        }
        return filteredStates;
    }

    /**
     * Helper method to compute sets of the sort
     * {s in S | Post(s) intersect T != {}}
     * given the set of states S.
     * @param states S
     * @return {s in S | Post(s) intersect T != {}}
     * */
    private Set<State> postIntersectStatesNotEmpty(Set<State> states) {
        Set<State> sub = new HashSet<>();
        for (State state : states) {
            Set<State> postSet = state.getPostStates(states, this.model);
            if (!Collections.disjoint(postSet, states)) {
                sub.add(state);
            }
        }
        return sub;
    }
}
