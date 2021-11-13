package model;

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
}
