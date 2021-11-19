package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit tests to test CTL formulae (without action indexing).
 * */
public class ModelCheckerCTLTests {

    @Test
    public void check_model_ap() {
        try {
            boolean result1 = TestHelper.check("model", "atomic_prop_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "atomic_prop_b", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_bool() {
        try {
            boolean result1 = TestHelper.check("model", "bool_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "bool_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_not() {
        try {
            boolean result1 = TestHelper.check("model", "not_a", null);
            assertFalse(result1);

            boolean result2 = TestHelper.check("model", "not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_and() {
        try {
            boolean result1 = TestHelper.check("model", "a_and_b", null);
            assertFalse(result1);

            boolean result2 = TestHelper.check("model", "a_and_not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_or() {
        try {
            boolean result1 = TestHelper.check("model", "a_or_b", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "a_or_c", null);
            assertTrue(result2);

            boolean result3 = TestHelper.check("model", "b_or_c", null);
            assertFalse(result3);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    // passing
    @Test
    public void check_model_until() {
        try {
            boolean result1 = TestHelper.check("model", "exists_a_until_d", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "exists_a_until_c", null);
            // no label c -> false
            assertFalse(result2);

            boolean result3 = TestHelper.check("model", "exists_b_until_c", null);
            // b doesn't hold in initial state -> false
            assertFalse(result3);

             boolean result4 = TestHelper.check("model", "forall_a_until_d", null);
             assertFalse(result4);

             boolean result5 = TestHelper.check("model", "forall_a_until_b", null);
             assertTrue(result5);

             boolean result6 = TestHelper.check("model", "forall_true_until_b", null);
             assertTrue(result6);

            boolean result7 = TestHelper.check("model", "forall_a_or_b_until_c", null);
            assertFalse(result7);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_next() {
        try {
            boolean result1 = TestHelper.check("model", "exists_next_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "exists_next_b", null);
            assertTrue(result2);

            boolean result3 = TestHelper.check("model", "exists_next_c", null);
            assertFalse(result3);

            boolean result4 = TestHelper.check("model", "forall_next_a", null);
            assertFalse(result4);

            boolean result5 = TestHelper.check("model", "forall_next_a_or_b", null);
            assertTrue(result5);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_always() {
        try {
            boolean result1 = TestHelper.check("model", "exists_always_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "exists_always_b", null);
            assertFalse(result2);

            boolean result3 = TestHelper.check("model", "exists_always_a_or_b", null);
            assertTrue(result3);

            boolean result4 = TestHelper.check("model", "forall_always_a", null);
            assertFalse(result4);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model_eventually() {
        try {
            boolean result1 = TestHelper.check("model", "exists_eventually_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model", "exists_eventually_c", null);
            assertFalse(result2);

            boolean result3 = TestHelper.check("model", "forall_eventually_a", null);
            assertTrue(result3);

            boolean result4 = TestHelper.check("model", "forall_eventually_a_and_b", null);
            assertTrue(result4);

             boolean result5 = TestHelper.check("model", "forall_eventually_c", null);
             assertFalse(result5);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
