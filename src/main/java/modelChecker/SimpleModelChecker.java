package modelChecker;

import formula.stateFormula.And;
import formula.stateFormula.StateFormula;
import model.Model;
import model.State;

import java.util.Set;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {

        // todo what do we do if the model has terminal states?

        // handle constraint
        if (constraint != null) {
            query = new And(constraint, query);
        }

        // todo convert to ENF

        // get satisfying set
        SatSetComputer satSetComputer = new SatSetComputer(model);
        Set<State> satSet = satSetComputer.computeSatSet(query, model.getStatesSet());

        // compare satSet to initial states
        boolean check = satSet.containsAll(model.getInitialStates());

        // model is valid
        if (check) {
            return true;
        }

        // model is not valid...
        // todo generate trace and save to instance variable
        System.out.println("Model is not valid whoopsie");
        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
