package modelChecker;

import java.io.IOException;

import static org.junit.Assert.*;

import org.junit.Test;

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
