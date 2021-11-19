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

        // todo remove redundant declarations.. just for debugging

        // compute the sat set for the state formula first
        Set<State> satSetOrig = computeSatSet(formula.stateFormula, states);

        // remove from the sat set the states that cannot be reached via the pre-actions
        Set<State> satSetPre = this.computeSatSetIn(satSetOrig, formula.getActions());

        // {s in S | Post(s) intersect Sat(formula) != {}}
        // all predecessors of the states in the sat set
        Set<State> satSetFiltered = this.postIntersectStatesNotEmpty(this.model.getStatesSet(), satSetPre);

        // todo revise idea:
        //  filter the sat set returned from the last call by all those states that are actually
        //  transitioning via the required action.
        //  i.e. basically do the reverse of what satSetPreActions does
        Set<State> filtered = this.computeSatSetOut(satSetFiltered, formula.getActions());

        return filtered;
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

        Set<State> satSetRight = this.computeSatSetIn(this.computeSatSet(formula.right, states), formula.getRightActions());
        Set<State> satSetLeft = this.computeSatSetOut(this.computeSatSet(formula.left, states), formula.getLeftActions());

        Set<State> E = new HashSet<>(satSetRight);
        Set<State> T = new HashSet<>(E);

        while (!E.isEmpty()) {
            Iterator<State> it = E.iterator();
            State sPrime = it.next();
            it.remove();

            // direct predecessors of sPrime that can reach sPrime via A action
            Set<State> preSPrime = sPrime.getPreStatesWithActions(model, formula.getLeftActions());

            for (State s : preSPrime) {

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
     * ALGORITHM:
     * T := Sat(phi)
     * while {s in T | Post(s) intersect T = {}} != {} do:
     *      let s in {s in T | Post(s) intersect T = {}};
     *      T := T w/o {s};
     * od;
     * SatSet = T;
     *
     * From the original sat set Sat(phi), remove al those states whose successors are not in the sat set.
     * By removing from the original sat set those states who violate the action preconditions, we can
     * follow the same approach and get our desired set.
     * */
    @Override
    public Set<State> visitAlways(Always formula, Set<State> states) {

        Set<State> satSet = computeSatSet(formula.stateFormula, states);
        // filter incoming
        satSet = this.computeSatSetIn(satSet, formula.getActions());
        // filter outgoing
        satSet = this.computeSatSetOut(satSet, formula.getActions());

        Set<State> E = setDifference(states, satSet);
        Set<State> T = new HashSet<>(satSet);

        HashMap<State, Integer> count = new HashMap<>();
        for (State s : satSet) {
            // successors with correct incoming actions
            Set<State> postS = this.computeSatSetIn(s.getPostStates(model), formula.getActions());
            // successors with correct outgoing actions
            postS = this.computeSatSetOut(postS, formula.getActions());
            count.put(s, postS.size());
        }

        while (!E.isEmpty()) {
            Iterator<State> it = E.iterator();
            State sPrime = it.next();
            it.remove();

            // direct predecessors of sPrime that can reach sPrime via A action
            Set<State> preSPrime = sPrime.getPreStatesWithActions(model, formula.getActions());
            for (State s : preSPrime) {
                if (T.contains(s)) {
                    count.put(s, count.get(s) - 1);
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

            if (state.isInit()) {
                retain = true;
            } else {

                for (Transition transition : incomingTransitions) {

                    // if transition contains correct action, then we're done
                    if (!Collections.disjoint(actions, transition.getActionsSet())) {
                        retain = true;
                        break;
                    }
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

    /**
     * Helper method to remove unreachable states during the UNTIL operation
     * */
    private Set<State> removeUnreachableStates(Set<State> sPrime, Set<State> satSetLeft, Set<State> satSetRight, Until formula) {
        Set<State> sPrimePrime = new HashSet<>();
        for (State state : sPrime) {
            Set<State> postStates = state.getPostStates(model);

            // should the current state be retained?
            boolean retain = false;

            // check all postStates to find if the actions match
            for (State postState : postStates) {

                // postState of sPrime could be in satSetRight
                if (satSetRight.contains(postState)) {

                    // incoming transitions into postState from state
                    for (Transition transition : postState.getIncomingTransitions(model)) {

                        // only include transitions between state and postState
                        boolean sourceMatch = transition.getSource().equals(state.getName());
                        boolean targetMatch = transition.getTarget().equals(postState.getName());

                        // do the actions of this transition include the actions of the formula?
                        boolean actionsMatch = true;
                        if (!formula.getRightActions().isEmpty()) {
                            actionsMatch = !Collections.disjoint(transition.getActionsSet(), formula.getRightActions());
                        }

                        // source must match state, target must match postState, actions must include at least one right action
                        if (sourceMatch && targetMatch && actionsMatch) {
                            retain = true;
                            // don't need to check any more transitions
                            break;
                        }
                    }
                    // already found a reason to retain -> no need to look at other post states
                    if (retain) {
                        break;
                    }
                }

                // postState of sPrime could be in satSetLeft itself
                if (satSetLeft.contains(postState)) {

                    // incoming transitions into postState from state
                    for (Transition transition : postState.getIncomingTransitions(model)) {

                        // only include transitions between state and postState
                        boolean sourceMatch = transition.getSource().equals(state.getName());
                        boolean targetMatch = transition.getTarget().equals(postState.getName());

                        // do the actions of this transition include the actions of the formula?
                        boolean actionsMatch = true;
                        if (!formula.getLeftActions().isEmpty()) {
                            actionsMatch = !Collections.disjoint(transition.getActionsSet(), formula.getLeftActions());
                        }

                        // source must match state, target must match postState, actions must include at least one left action
                        if (sourceMatch && targetMatch && actionsMatch) {
                            retain = true;
                            // don't need to check any more transitions
                            break;
                        }
                    }
                    // already found a reason to retain -> no need to look at other postStates
                    if (retain) {
                        break;
                    }
                }
            }

            // found a reason to retain -> add to sPrimePrime
            if (retain) {
                sPrimePrime.add(state);
            }
        }
        return sPrimePrime;
    }
}
