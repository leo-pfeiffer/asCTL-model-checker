package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import model.Model;

public class ModelCheckerTest {

    private boolean check_helper(String modelName, String formulaName, String constraintName) throws IOException {
        try {
            Model model = Model.parseModel("src/test/resources/test-models/" + modelName + ".json");
            StateFormula query = new FormulaParser("src/test/resources/test-formulae/" + formulaName + ".json").parse();

            StateFormula fairnessConstraint = null;
            if (constraintName != null) {
                fairnessConstraint = new FormulaParser("src/test/resources/test-constraints" + constraintName + ".json").parse();
            }

            ModelChecker mc = new SimpleModelChecker();

            return mc.check(model, fairnessConstraint, query);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
            return false;
        }
    }

    /*
     * An example of how to set up and call the model building methods and make
     * a call to the model checker itself. The contents of model.json,
     * constraint1.json and ctl.json are just examples, you need to add new
     * models and formulas for the mutual exclusion task.
     */
    @Test
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

    @Test
    public void check_model2_ap() {
        try {
            boolean result1 = this.check_helper("model2", "atomic_prop_a", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "atomic_prop_b", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_bool() {
        try {
            boolean result1 = this.check_helper("model2", "bool_true", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "bool_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_not() {
        try {
            boolean result1 = this.check_helper("model2", "not_a", null);
            assertFalse(result1);

            boolean result2 = this.check_helper("model2", "not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_and() {
        try {
            boolean result1 = this.check_helper("model2", "a_and_b", null);
            assertFalse(result1);

            boolean result2 = this.check_helper("model2", "a_and_not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_or() {
        try {
            boolean result1 = this.check_helper("model2", "a_or_b", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "a_or_c", null);
            assertTrue(result2);

            boolean result3 = this.check_helper("model2", "b_or_c", null);
            assertFalse(result3);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_until() {
        try {
            boolean result1 = this.check_helper("model2", "exists_a_until_b", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "exists_a_until_c", null);
            // no label c -> false
            assertFalse(result2);

            boolean result3 = this.check_helper("model2", "exists_b_until_c", null);
            // b doesn't hold in initial state -> false
            assertFalse(result3);

            boolean result4 = this.check_helper("model2", "forall_a_until_b", null);
            assertTrue(result4);

            boolean result5 = this.check_helper("model2", "forall_true_until_b", null);
            assertTrue(result5);

            // todo not passing
            boolean result6 = this.check_helper("model2", "forall_a_or_b_until_c", null);
            assertFalse(result6);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_next() {
        try {
            boolean result1 = this.check_helper("model2", "exists_next_a", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "exists_next_b", null);
            assertTrue(result2);

            boolean result3 = this.check_helper("model2", "exists_next_c", null);
            assertFalse(result3);

            boolean result4 = this.check_helper("model2", "forall_next_a", null);
            assertFalse(result4);

            boolean result5 = this.check_helper("model2", "forall_next_a_or_b", null);
            assertTrue(result5);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_always() {
        try {
            boolean result1 = this.check_helper("model2", "exists_always_a", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "exists_always_b", null);
            assertFalse(result2);

            boolean result3 = this.check_helper("model2", "exists_always_a_or_b", null);
            assertTrue(result3);

            boolean result4 = this.check_helper("model2", "forall_always_a", null);
            assertFalse(result4);

            // todo not passing
            boolean result5 = this.check_helper("model2", "forall_always_a_or_b", null);
            assertTrue(result5);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model2_eventually() {
        try {
            boolean result1 = this.check_helper("model2", "exists_eventually_a", null);
            assertTrue(result1);

            boolean result2 = this.check_helper("model2", "exists_eventually_c", null);
            assertFalse(result2);

            boolean result3 = this.check_helper("model2", "forall_eventually_a", null);
            assertTrue(result3);

            boolean result4 = this.check_helper("model2", "forall_eventually_a_and_b", null);
            assertTrue(result4);

             boolean result5 = this.check_helper("model2", "forall_eventually_c", null);
             assertFalse(result5);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
