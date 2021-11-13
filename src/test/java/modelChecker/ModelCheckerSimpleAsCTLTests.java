package modelChecker;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

/**
 * Unit tests to test simple asCTL formulae.
 * */
public class ModelCheckerSimpleAsCTLTests {

    // passing
    @Test
    public void model4_exists_next() {
        try {
             boolean result1 = TestHelper.check("model4", "asCTL_exists_next_true", null);
             assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_exists_next_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // passing
    @Test
    public void model4_forall_next() {
        try {
            boolean result1 = TestHelper.check("model4", "asCTL_forall_next_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_forall_next_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // passing
    @Test
    public void model4_exists_always() {
        try {
            boolean result1 = TestHelper.check("model4", "asCTL_exists_always_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_exists_always_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // todo not passing -> uses until
    @Test
    public void model4_forall_always() {
        try {
            // todo not passing
            boolean result1 = TestHelper.check("model4", "asCTL_forall_always_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_forall_always_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // todo passing but that's probably just a coincidence
    @Test
    public void model4_exists_until() {
        try {
            boolean result1 = TestHelper.check("model4", "asCTL_exists_until_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_exists_until_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // todo not passing -> uses until
    @Test
    public void model4_forall_until() {
        try {
            boolean result1 = TestHelper.check("model4", "asCTL_forall_until_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "asCTL_forall_until_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
