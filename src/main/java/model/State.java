package model;

import javax.swing.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 
 * */
public class State {
    private boolean init;
    private String name;
    private String [] label;
	
    /**
     * Is state an initial state
     * @return boolean init 
     * */
    public boolean isInit() {
	return init;
    }
	
    /**
     * Returns the name of the state
     * @return String name 
     * */
    public String getName() {
	return name;
    }
	
    /**
     * Returns the labels of the state
     * @return Array of string labels
     * */
    public String[] getLabel() {
	return label;
    }

    /**
     * For a model, get the incoming transitions into this state.
     * @param model Model to get the transitions from
     * @return Set<Transition> incoming transitions
     */
    public Set<Transition> getIncomingTransitions(Model model) {
        Set<Transition> incomingTransitions = new HashSet<>();
        for (Transition transition : model.getTransitions()) {
            if (transition.getTarget().equals(this.name)) {
                incomingTransitions.add(transition);
            }
        }
        return incomingTransitions;
    }

    /**
     * For a model, get the outgoing transitions from this state.
     * @param model Model to get the transitions from
     * @return Set<Transition> outgoing transitions
     */
    public Set<Transition> getOutgoingTransitions(Model model) {
        Set<Transition> outgoingTransitions = new HashSet<>();
        for (Transition transition : model.getTransitions()) {
            if (transition.getSource().equals(this.name)) {
                outgoingTransitions.add(transition);
            }
        }
        return outgoingTransitions;
    }

    /**
     * Get all post states (successors) of this state
     * @param model Model to get the states from
     * @return Set of post states
     */
    public Set<State> getPostStates(Model model) {
        Set<State> postStates = new HashSet<>();
        for (Transition transition: this.getOutgoingTransitions(model)) {
            postStates.add(model.getStateByName(transition.getTarget()));
        }
        return postStates;
    }

    public Set<State> getPostStatesWithActions(Model model, Set<String> actions) {
        return null;
    }

    /**
     * Get all pre states (predecessors) of this state
     * @param model Model to get the states from
     * @return Set of pre states
     */
    public Set<State> getPreStates(Model model) {
        Set<State> preStates = new HashSet<>();
        for (Transition transition: this.getIncomingTransitions(model)) {
            preStates.add(model.getStateByName(transition.getSource()));
        }
        return preStates;
    }

    public Set<State> getPreStatesWithActions(Model model, Set<String> actions) {
        Set<State> preStates = this.getPreStates(model);
        if (actions.isEmpty()) {
            return preStates;
        }
        Set<State> toRemove = new HashSet<>();
        for (State s : preStates) {
            for (Transition t: s.getOutgoingTransitions(model)) {
                if (t.getTarget().equals(this.name)) {
                    if (Collections.disjoint(t.getActionsSet(), actions)) {
                        toRemove.add(s);
                    }
                }
            }
        }
        preStates.removeAll(toRemove);
        return preStates;
    }

    public Set<String> getOutgoingActions(Model model) {
        Set<Transition> out = this.getOutgoingTransitions(model);
        Set<String> actions = new HashSet<>();
        for (Transition t: out) {
            actions.addAll(t.getActionsSet());
        }
        return actions;
    }

    public Set<String> getIncomingActions(Model model) {
        Set<Transition> in = this.getIncomingTransitions(model);
        Set<String> actions = new HashSet<>();
        for (Transition t: in) {
            actions.addAll(t.getActionsSet());
        }
        return actions;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.name).append(" : {");
        for (int i = 0; i < this.label.length; i++) {
            stringBuilder.append(this.label[i]);
            if (i != this.label.length - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }
}
