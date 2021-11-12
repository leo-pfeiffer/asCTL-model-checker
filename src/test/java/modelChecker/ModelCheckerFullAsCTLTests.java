package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import formula.FormulaParser;
import formula.stateFormula.StateFormula;
import model.Model;

public class ModelCheckerFullAsCTLTests {

    @Test
    public void model1_ctl1_constraint1() {
        try {
            boolean result = TestHelper.check("model1", "ctl1", "constraint1");
            // todo not passing
            assertTrue(result);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
