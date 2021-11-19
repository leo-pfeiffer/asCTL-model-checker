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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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

        // remove from the sat set the states that cannot be reached via the pre-actions
        satSet = this.computeSatSetIn(satSet, formula.getActions());

        // {s in S | Post(s) intersect Sat(formula) != {}}
        // all predecessors of the states in the sat set
        satSet = this.postIntersectStatesNotEmpty(this.model.getStatesSet(), satSet);

        return this.computeSatSetOut(satSet, formula.getActions());
    }

    /**
     * Sat Set for Until.
     *
     * See Algorithm 2 in the report.
     * */
    @Override
    public Set<State> visitUntil(Until formula, Set<State> states) {

        // satisfaction set of left and right formulae filtered by the actions
        Set<State> satSetRight = this.computeSatSetIn(this.computeSatSet(formula.right, states), formula.getRightActions());
        Set<State> satSetLeft = this.computeSatSetOut(this.computeSatSet(formula.left, states), formula.getLeftActions());

        // helper set from which contenders for T are draws
        Set<State> E = new HashSet<>(satSetRight);
        // all states in satSetRight initially support formula
        Set<State> T = new HashSet<>(E);

        // repeat until no more contenders
        while (!E.isEmpty()) {

            // pick a contender and remove it
            Iterator<State> it = E.iterator();
            State sPrime = it.next();
            it.remove();

            // direct predecessors of sPrime that can reach sPrime via A action
            Set<State> preSPrime = sPrime.getPreStatesWithActions(model, formula.getLeftActions());

            for (State s : preSPrime) {
                // must also satisfy left formula and not already in T
                if (satSetLeft.contains(s) && !T.contains(s))
                    E.add(s);
                    T.add(s);
            }
        }
        return T;
    }

    /**
     * Sat Set for Always.
     *
     * See Algorithm 3 in the report.
     * */
    @Override
    public Set<State> visitAlways(Always formula, Set<State> states) {

        // satisfaction set barring the actions
        Set<State> satSetOrig = computeSatSet(formula.stateFormula, states);

        // filter incoming
        Set<State> satSet = this.computeSatSetIn(satSetOrig, formula.getActions());

        for (State s : model.getInitialStates()) {
            if (satSetOrig.contains(s)) {
                satSet.add(s);
            }
        }

        // filter outgoing
        satSet = this.computeSatSetOut(satSet, formula.getActions());

        // unvisited states that *do not* satisfy the formula
        Set<State> E = setDifference(states, satSet);
        // superset of final satisfaction set with contender states -> will iteratively remove from this
        Set<State> T = new HashSet<>(satSet);

        // hashmap to keep track of number of successors of each state
        HashMap<State, Integer> count = new HashMap<>();
        for (State s : satSet) {
            // successors with correct incoming actions
            Set<State> postS = this.computeSatSetIn(s.getPostStates(model), formula.getActions());
            // successors with correct outgoing actions
            postS = this.computeSatSetOut(postS, formula.getActions());
            count.put(s, postS.size());
        }

        // repeat until no more contenders
        while (!E.isEmpty()) {

            // pick a contender and remove it
            Iterator<State> it = E.iterator();
            State sPrime = it.next();
            it.remove();

            // direct predecessors of sPrime that can reach sPrime via A action
            Set<State> preSPrime = sPrime.getPreStatesWithActions(model, formula.getActions());
            for (State s : preSPrime) {
                if (T.contains(s)) {
                    count.put(s, count.get(s) - 1);

                    // s has no successors via A in T -> remove
                    if (count.get(s) == 0) {
                        T.remove(s);
                        E.add(s);
                    }
                }
            }
        }

        return T;
    }

    /**
     * From a set of states, get the ones that satisfy the pre-actions.
     * Basically, this retains all states that can be reached via the pre-actions.
     * @param states set of states
     * @param actions set of actions from the pre-actions
     * @return set of states that satisfy the pre-actions
     * */
    private Set<State> computeSatSetIn(Set<State> states, Set<String> actions) {

        // nothing to do if there are no pre-actions
        if (actions.isEmpty()) {
            return states;
        }

        // subset of states that is reachable via the pre-actions
        Set<State> filteredStates = new HashSet<>();
        for (State state : states) {

            // get the incoming transitions into the current state
            Set<Transition> incomingTransitions = state.getIncomingTransitions(model);

            // retain only those transitions that are in the set of actions
            boolean retain = false;

            for (Transition transition : incomingTransitions) {
                // if transition contains correct action, then we're done
                if (!Collections.disjoint(actions, transition.getActionsSet())) {
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
     * Retains all states that have any outgoing transitions via the post-actions.
     * @param states set of states
     * @param actions set of post-actions
     * @return set of states that satisfy the post-actions
     * */
    private Set<State> computeSatSetOut(Set<State> states, Set<String> actions) {

        // nothing to do if there are no post-actions
        if (actions.isEmpty()) {
            return states;
        }

        // subset of states that has includes the post-actions
        Set<State> filteredStates = new HashSet<>();

        for (State state : states) {

            // get the outgoing transitions from the current state
            Set<Transition> outgoingTransitions = state.getOutgoingTransitions(model);

            // retain only those transitions that are in the set of actions
            boolean retain = false;
            for (Transition transition : outgoingTransitions) {

                // if transition contains correct action, then we're done
                if (!Collections.disjoint(actions, transition.getActionsSet())) {
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
     * given the set of all states S and the intersection set T.
     * @param S S
     * @param T T
     * @return {s in S | Post(s) intersect T != {}}
     * */
    private Set<State> postIntersectStatesNotEmpty(Set<State> S, Set<State> T) {
        Set<State> sPrime = new HashSet<>();
        for (State state : S) {
            Set<State> postSet = state.getPostStates(this.model);
            if (!Collections.disjoint(postSet, T)) {
                sPrime.add(state);
            }
        }
        return sPrime;
    }
}
