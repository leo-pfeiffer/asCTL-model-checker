package modelChecker;

import formula.ENFConverter;
import formula.stateFormula.And;
import formula.stateFormula.StateFormula;
import model.Model;
import model.State;

import java.util.Set;

public class SimpleModelChecker implements ModelChecker {

    @Override
    public boolean check(Model model, StateFormula constraint, StateFormula query) {

        // handle constraint
        if (constraint != null) {
            query = new And(constraint, query);
        }

        // convert the formula ENF
        ENFConverter enfConverter = new ENFConverter();
        StateFormula enf = enfConverter.convertToENF(query);

        // get satisfying set
        SatSetComputer satSetComputer = new SatSetComputer(model);
        Set<State> satSet = satSetComputer.computeSatSet(enf, model.getStatesSet());

        // compare satSet to initial states
        boolean check = satSet.containsAll(model.getInitialStates());

        // model is valid
        if (check) {
            return true;
        }

        // model is not valid.

        System.out.println("Model is not valid");

        CounterexampleGenerator counterexampleGenerator = new CounterexampleGenerator();
        // todo not implemented!
        // counterexampleGenerator.generate();

        return false;
    }

    @Override
    public String[] getTrace() {
        // TODO Auto-generated method stub
        return null;
    }

}
