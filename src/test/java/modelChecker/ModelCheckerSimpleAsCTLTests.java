package modelChecker;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests to test simple asCTL formulae.
 * */
public class ModelCheckerSimpleAsCTLTests {

    @Test
    public void model4_next() {
        try {
             boolean result1 = TestHelper.check("model4", "asCTL_next_true", null);
             assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_next_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }
}
