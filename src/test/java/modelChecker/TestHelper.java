package modelChecker;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import model.Model;

import java.io.IOException;

import static org.junit.Assert.fail;

public class TestHelper {

    public static boolean check(String modelName, String formulaName, String constraintName) throws IOException {
        try {
            Model model = Model.parseModel("src/test/resources/test-models/" + modelName + ".json");
            StateFormula query = new FormulaParser("src/test/resources/test-formulae/" + formulaName + ".json").parse();

            StateFormula fairnessConstraint = null;
            if (constraintName != null) {
                fairnessConstraint = new FormulaParser("src/test/resources/test-constraints/" + constraintName + ".json").parse();
            }

            ModelChecker mc = new SimpleModelChecker();

            return mc.check(model, fairnessConstraint, query);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
            return false;
        }
    }
}
