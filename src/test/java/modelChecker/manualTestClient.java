package modelChecker;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import model.Model;

import java.io.IOException;

import static org.junit.Assert.fail;

public class manualTestClient {

    public static void main(String[] args) {
        manualTestClient test = new manualTestClient();
        test.buildAndCheckModel();
        int x = 0;
    }

    /*
     * An example of how to set up and call the model building methods and make
     * a call to the model checker itself. The contents of model.json,
     * constraint1.json and ctl.json are just examples, you need to add new
     * models and formulas for the mutual exclusion task.
     */
    public void buildAndCheckModel() {
        try {
            Model model = Model.parseModel("src/test/resources/model1.json");

            StateFormula fairnessConstraint = new FormulaParser("src/test/resources/constraint1.json").parse();
            StateFormula query = new FormulaParser("src/test/resources/ctl1.json").parse();

            ModelChecker mc = new SimpleModelChecker();

            // TO IMPLEMENT
            // assertTrue(mc.check(model, fairnessConstraint, query));
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
