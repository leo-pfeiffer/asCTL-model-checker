package model;

import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;

/**
 * A model is consist of states and transitions
 */
public class Model {
    State[] states;
    Transition[] transitions;
    // todo Set<State> initialStates;
    // todo Set<State> stateSet;

    public static Model parseModel(String filePath) throws IOException {
        Gson gson = new Gson();
        Model model = gson.fromJson(new FileReader(filePath), Model.class);
        for (Transition t : model.transitions) {
            System.out.println(t);
        }

        return model;
    }

    /**
     * Returns the list of the states
     * 
     * @return list of state for the given model
     */
    public State[] getStates() {
        return states;
    }

    /**
     * Returns the states in a set.
     *
     * @return set of states for the given model
     */
    public Set<State> getStatesSet() {
        // todo calculate this once at the beginning instead of on the fly
        Set<State> states = new HashSet<>();
        Collections.addAll(states, this.states);
        return states;
    }

    /**
     * Returns the list of transitions
     * 
     * @return list of transition for the given model
     */
    public Transition[] getTransitions() {
        return transitions;
    }

    public State getStateByName(String name) {
        for (State s : states) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }

    public Set<State> getInitialStates() {
        // todo calculate this once at the beginning instead of on the fly
        Set<State> initialStates = new HashSet<>();
        for (State s : states) {
            if (s.isInit()) {
                initialStates.add(s);
            }
        }
        return initialStates;
    }
}
