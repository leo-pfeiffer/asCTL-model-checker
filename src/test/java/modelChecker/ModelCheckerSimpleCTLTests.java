package modelChecker;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

/**
 * Unit tests to test simple CTL formulae (without action indexing).
 * */
public class ModelCheckerSimpleCTLTests {

    @Test
    public void check_model4_ap() {
        try {
            boolean result1 = TestHelper.check("model4", "atomic_prop_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "atomic_prop_b", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_bool() {
        try {
            boolean result1 = TestHelper.check("model4", "bool_true", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "bool_false", null);
            assertFalse(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_not() {
        try {
            boolean result1 = TestHelper.check("model4", "not_a", null);
            assertFalse(result1);

            boolean result2 = TestHelper.check("model4", "not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_and() {
        try {
            boolean result1 = TestHelper.check("model4", "a_and_b", null);
            assertFalse(result1);

            boolean result2 = TestHelper.check("model4", "a_and_not_b", null);
            assertTrue(result2);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_or() {
        try {
            boolean result1 = TestHelper.check("model4", "a_or_b", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "a_or_c", null);
            assertTrue(result2);

            boolean result3 = TestHelper.check("model4", "b_or_c", null);
            assertFalse(result3);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_until() {
        try {
            boolean result1 = TestHelper.check("model4", "exists_a_until_d", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "exists_a_until_c", null);
            // no label c -> false
            assertFalse(result2);

            boolean result3 = TestHelper.check("model4", "exists_b_until_c", null);
            // b doesn't hold in initial state -> false
            assertFalse(result3);

             boolean result4 = TestHelper.check("model4", "forall_a_until_d", null);
             assertFalse(result4);

            // todo does not terminate
            // boolean result5 = TestHelper.check("model4", "forall_a_until_b", null);
            // assertTrue(result5);

            // todo does not terminate
            // boolean result6 = TestHelper.check("model4", "forall_true_until_b", null);
            // assertTrue(result6);

            boolean result7 = TestHelper.check("model4", "forall_a_or_b_until_c", null);
            assertFalse(result7);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_next() {
        try {
            boolean result1 = TestHelper.check("model4", "exists_next_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "exists_next_b", null);
            assertTrue(result2);

            boolean result3 = TestHelper.check("model4", "exists_next_c", null);
            assertFalse(result3);

            boolean result4 = TestHelper.check("model4", "forall_next_a", null);
            assertFalse(result4);

            boolean result5 = TestHelper.check("model4", "forall_next_a_or_b", null);
            assertTrue(result5);
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_always() {
        try {
            boolean result1 = TestHelper.check("model4", "exists_always_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "exists_always_b", null);
            assertFalse(result2);

            boolean result3 = TestHelper.check("model4", "exists_always_a_or_b", null);
            assertTrue(result3);

            boolean result4 = TestHelper.check("model4", "forall_always_a", null);
            assertFalse(result4);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

    @Test
    public void check_model4_eventually() {
        try {
            boolean result1 = TestHelper.check("model4", "exists_eventually_a", null);
            assertTrue(result1);

            boolean result2 = TestHelper.check("model4", "exists_eventually_c", null);
            assertFalse(result2);

            boolean result3 = TestHelper.check("model4", "forall_eventually_a", null);
            assertTrue(result3);

            boolean result4 = TestHelper.check("model4", "forall_eventually_a_and_b", null);
            assertTrue(result4);

             boolean result5 = TestHelper.check("model4", "forall_eventually_c", null);
             assertFalse(result5);

        } catch (IOException e) {
            e.printStackTrace();
            fail(e.toString());
        }
    }

}
