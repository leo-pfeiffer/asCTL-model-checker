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
        Set<State> satSetPre = this.satSetPreActions(satSetOrig, formula.getActions());

        // {s in S | Post(s) intersect Sat(formula) != {}}
        // all predecessors of the states in the sat set
        Set<State> satSetFiltered = this.postIntersectStatesNotEmpty(this.model.getStatesSet(), satSetPre);

        // todo revise idea:
        //  filter the sat set returned from the last call by all those states that are actually
        //  transitioning via the required action.
        //  i.e. basically do the reverse of what satSetPreActions does
        Set<State> filtered = this.satSetPostActions(satSetFiltered, formula.getActions());

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

            // satLeftPrime = {s in satLeft w/o T}
            // states from satLeft that are not in finalSatSet (yet)
            Set<State> satLeftPrime = setDifference(satSetLeft, finalSatSet);

            // {s in satLeftPrime | Post(s) intersect T != {}}
            // remove those from satLeftPrime that don't have successors in finalSatSet
            // todo check if this is correct
            Set<State> sub = this.postIntersectStatesNotEmpty(satLeftPrime, finalSatSet);

            if (sub.isEmpty()) {
                break;
            }

            // T union {s}
            finalSatSet.addAll(sub);
        }

        return finalSatSet;
    }

    //@Override
    public Set<State> visitUntilUpdated(Until formula, Set<State> states) {

        // sat sets for left and right ignoring the action index
        Set<State> satSetLeft = computeSatSet(formula.left, states);
        Set<State> satSetRight = computeSatSet(formula.right, states);

        // consider action index
        satSetLeft = this.satSetPostActions(satSetLeft, formula.getLeftActions());
        satSetRight = this.satSetPreActions(satSetRight, formula.getRightActions());

        // todo don't know if I need to do this
        //  basically, this removes all states from satSetLeft that cannot reach satSetRight via left actions
//        if (!satSetLeft.isEmpty()) {
//            Set<State> remove = new HashSet<>();
//            for (State s : satSetLeft) {
//                boolean retain = false;
//                for (State sr : satSetRight) {
//                    for (Transition t : s.getOutgoingTransitions(model)) {
//                        boolean sourceMatch = t.getSource().equals(s.getName());
//                        boolean targetMatch = t.getTarget().equals(sr.getName());
//                        boolean actionsMatch = !Collections.disjoint(t.getActionsSet(), formula.getLeftActions());
//                        if (sourceMatch && targetMatch && actionsMatch) {
//                            retain = true;
//                            break;
//                        }
//                    }
//                    if (retain) {
//                        break;
//                    }
//                }
//                if (!retain) {
//                    remove.add(s);
//                }
//            }
//            satSetLeft.removeAll(remove);
//        }

        // T := Sat(right)
        Set<State> finalSatSet = new HashSet<>(satSetRight);

        // while {s in Sat(left) w/o T | Post(s) intersect T != {}} != {} do
        while (true) {

            // satLeftPrime = {s in satLeft w/o T}
            Set<State> satLeftPrime = setDifference(satSetLeft, finalSatSet);

            // states to remove from satLeftPrime because the actions don't match
            Set<State> sub = new HashSet<>();

            for (State state : satLeftPrime) {
                Set<State> postStates = state.getPostStates(model);

                // should the current state be retained?
                boolean retain = false;

                // check all postStates to find if the actions match
                for (State postState : postStates) {
                    // postStates of satSetRight states must be reachable by right actions
                    if (satSetRight.contains(postState) && !formula.getRightActions().isEmpty()) {

                        // incoming transitions into postState from state
                        for (Transition transition : postState.getIncomingTransitions(model)) {

                            boolean sourceMatch = transition.getSource().equals(state.getName());
                            boolean targetMatch = transition.getTarget().equals(postState.getName());
                            boolean actionsMatch = !Collections.disjoint(transition.getActionsSet(), formula.getRightActions());

                            // source must match state, target must match postState, actions must include at least one right action
                            if (sourceMatch && targetMatch && actionsMatch) {
                                retain = true;
                                break;
                            }
                        }
                        // already found a reason to retain -> no need to look at other post states
                        if (retain) {
                            break;
                        }
                    }

                    // postStates of satSetLeft states must be reachable by left actions
                    if (satSetLeft.contains(postState) && !formula.getRightActions().isEmpty()) {

                        // incoming transitions into postState from state
                        for (Transition transition : postState.getIncomingTransitions(model)) {
                            boolean sourceMatch = transition.getSource().equals(state.getName());
                            boolean targetMatch = transition.getTarget().equals(postState.getName());
                            boolean actionsMatch = !Collections.disjoint(transition.getActionsSet(), formula.getLeftActions());

                            // source must match state, target must match postState, actions must include at least one left action
                            if (sourceMatch && targetMatch && actionsMatch) {
                                retain = true;
                                break;
                            }
                        }
                        // already found a reason to retain -> no need to look at other postStates
                        if (retain) {
                            break;
                        }
                    }
                }

                // no reason to retain -> add to sub for removal
                if (!retain) {
                    sub.add(state);
                }
            }

            satLeftPrime.removeAll(sub);

            if (satLeftPrime.isEmpty()) {
                break;
            }

            finalSatSet.addAll(satLeftPrime);
        }

        return finalSatSet;
    }

    // Algorithm 15... maybe an alternative for until?
    public Set<State> visitUntilAlternative(Until formula, Set<State> states) {
        Set<State> satSetLeft = computeSatSet(formula.left, states);
        Set<State> E = computeSatSet(formula.right, states);
        Set<State> T = new HashSet<>(E);

        while (!E.isEmpty()) {
            State sPrime = E.iterator().next();
            E.remove(sPrime);
            for (State s : sPrime.getPreStates(model)) {
                if (setDifference(satSetLeft, T).contains(s)) {
                    E.add(sPrime);
                    T.add(sPrime);
                }
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

        // todo remove redundant declarations.. just for debugging

        Set<State> satSetOrig = computeSatSet(formula.stateFormula, states);

        // remove from the sat set those states that cannot be reached with the actions
        Set<State> satSetPost = this.satSetPostActions(satSetOrig, formula.getActions());

        // T := Sat(phi)
        // Set<State> finalSatSet = new HashSet<>(satSetPre);
        Set<State> finalSatSet = new HashSet<>(satSetPost);
        Set<State> sub = new HashSet<>();

        while(true) {

            // {s in T | Post(s) ∩ T = {}};
            for (State state : finalSatSet) {
                Set<State> postSet = state.getPostStates(model);
                if (Collections.disjoint(postSet, finalSatSet)) {
                    sub.add(state);
                }
                // even if the post set is in the sat set, remove it if we can't reach that successor using the post-actions
                else if (!formula.getActions().isEmpty()) {
                    boolean retain = false;
                    for (State postState : setIntersection(postSet, finalSatSet)) {
                        // transitions from state to postState
                        for (Transition transition : state.getOutgoingTransitions(model)) {
                            // only consider transitions that lead to postState
                            if (transition.getTarget().equals(postState.getName())) {
                                // if we can get from the state to the postState using the post-actions
                                if (!Collections.disjoint(transition.getActionsSet(), formula.getActions())) {
                                    retain = true;
                                    break;
                                }
                            }
                        }
                        if (retain) {
                            break;
                        }
                    }
                    if (!retain) {
                        sub.add(state);
                    }
                }
            }

            if (sub.isEmpty()) {
                break;
            }

            // T := T w/o {s}
            finalSatSet.removeAll(sub);

            if (finalSatSet.isEmpty()) {
                break;
            }

        }
        return finalSatSet;
    }

    // Algorithm 16... maybe an alternative?
    public Set<State> visitAlwaysAlternative(Always formula, Set<State> states) {
        Set<State> satSet = computeSatSet(formula.stateFormula, states);
        Set<State> E = setDifference(states, satSet);
        Set<State> T = new HashSet<>(satSet);
        HashMap<State, Integer> count = new HashMap<>();

        for (State state : satSet) {
            count.put(state, state.getPostStates(model).size());
        }

        while (!E.isEmpty()) {
            State sPrime = E.iterator().next();
            E.remove(sPrime);
            Set<State> removeFromT = new HashSet<>();
            for (State state : T) {
                count.put(state, count.get(state) - 1);
                if (count.get(state) == 0) {
                    removeFromT.add(state);
                    E.add(state);
                }
            }
            T.removeAll(removeFromT);
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
    private Set<State> satSetPreActions(Set<State> states, Set<String> actions) {

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
     * Retains all states whose has any outgoing transitions that satisfy the post-actions.
     * @param states set of states
     * @param actions set of post-actions
     * @return set of states that satisfy the post-actions
     * */
    private Set<State> satSetPostActions(Set<State> states, Set<String> actions) {

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
        Set<State> sub = new HashSet<>();
        for (State state : S) {
            Set<State> postSet = state.getPostStates(this.model);
            if (!Collections.disjoint(postSet, T)) {
                sub.add(state);
            }
        }
        return sub;
    }
}
