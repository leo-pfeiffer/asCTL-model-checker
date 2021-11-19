package formula;

import formula.pathFormula.Always;
import formula.pathFormula.Eventually;
import formula.pathFormula.Next;
import formula.pathFormula.Until;
import formula.stateFormula.And;
import formula.stateFormula.AtomicProp;
import formula.stateFormula.BoolProp;
import formula.stateFormula.ForAll;
import formula.stateFormula.Not;
import formula.stateFormula.Or;
import formula.stateFormula.StateFormula;
import formula.stateFormula.ThereExists;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class ENFConverterTest {
    @Test
    public void testConvertAtomicProp() {
        ENFConverter converter = new ENFConverter();
        AtomicProp formula = new AtomicProp("p");
        StateFormula enf = converter.convertToENF(formula);
        assert enf.toString().equals(formula.toString());
    }

    @Test
    public void testConvertBoolProp() {
        ENFConverter converter = new ENFConverter();

        BoolProp formulaTrue = new BoolProp(true);
        BoolProp formulaFalse = new BoolProp(false);

        StateFormula expectedTrue = new BoolProp(true);
        StateFormula expectedFalse = new Not(new BoolProp(true));

        StateFormula enfTrue = converter.convertToENF(formulaTrue);
        StateFormula enfFalse = converter.convertToENF(formulaFalse);

        assert enfTrue.toString().equals(expectedTrue.toString());
        assert enfFalse.toString().equals(expectedFalse.toString());
    }

    @Test
    public void testConvertAndSimple() {
        ENFConverter converter = new ENFConverter();

        AtomicProp p = new AtomicProp("p");
        AtomicProp q = new AtomicProp("q");

        StateFormula formula = new And(p,q);

        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(formula.toString());
    }

    @Test
    public void testConvertAndNested() {
        ENFConverter converter = new ENFConverter();

        BoolProp p = new BoolProp(true);
        BoolProp q = new BoolProp(false);

        StateFormula formula = new And(p,q);
        StateFormula expected = new And(new BoolProp(true), new Not(new BoolProp(true)));

        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertNot() {
        ENFConverter converter = new ENFConverter();

        AtomicProp p = new AtomicProp("p");

        StateFormula formula = new Not(p);
        StateFormula expected = new Not(new AtomicProp("p"));
        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertOr() {
        ENFConverter converter = new ENFConverter();

        AtomicProp p = new AtomicProp("p");
        AtomicProp q = new AtomicProp("q");

        StateFormula formula = new Or(p,q);

        Not neg1 = new Not(new AtomicProp("p"));
        Not neg2 = new Not(new AtomicProp("q"));

        StateFormula expected = new Not(new And(neg1, neg2));
        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertForAllNext() {
        ENFConverter converter = new ENFConverter();

        // empty actions
        AtomicProp ap = new AtomicProp("p");
        Next next = new Next(ap, new HashSet<String>());
        StateFormula formula = new ForAll(next);
        StateFormula expected = new Not(new ThereExists(new Next(new Not(ap), new HashSet<String>())));
        StateFormula enf = converter.convertToENF(formula);
        assert enf.toString().equals(expected.toString());

        // include actions
        Set<String> actions = new HashSet<>();
        actions.add("a");
        actions.add("b");
        next = new Next(ap, actions);
        formula = new ForAll(next);
        expected = new Not(new ThereExists(new Next(new Not(ap), actions)));
        enf = converter.convertToENF(formula);
        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertForAllUntil() {
        ENFConverter converter = new ENFConverter();

        // empty actions
        AtomicProp p = new AtomicProp("p");
        AtomicProp q = new AtomicProp("q");
        Set<String> actions = new HashSet<>();
        StateFormula formula = new ForAll(new Until(p, q, actions, actions));

        StateFormula untilLeft = new Not(q);
        StateFormula untilRight = new And(new Not(p), new Not(q));
        StateFormula until = new Not(new ThereExists(new Until(untilLeft, untilRight, actions, actions)));
        StateFormula right = new Not(new ThereExists(new Always(new Not(q), actions)));
        StateFormula expected = new And(until, right);

        StateFormula enf = converter.convertToENF(formula);
        assert enf.toString().equals(expected.toString());

        // include actions
        Set<String> preActions = new HashSet<>();
        preActions.add("a");
        preActions.add("b");
        Set<String> postActions = new HashSet<>();
        postActions.add("c");
        postActions.add("d");
        formula = new ForAll(new Until(p, q, preActions, postActions));

        until = new Not(new ThereExists(new Until(untilLeft, untilRight, preActions, postActions)));
        right = new Not(new ThereExists(new Always(new Not(q), postActions)));
        expected = new And(until, right);
        enf = converter.convertToENF(formula);
        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertForAllAlways() {
        ENFConverter converter = new ENFConverter();

        // empty actions
        AtomicProp ap = new AtomicProp("p");
        Always always = new Always(ap, new HashSet<String>());
        StateFormula formula = new ForAll(always);

        Until inner = new Until(new BoolProp(true), ap, new HashSet<String>(), new HashSet<String>());
        StateFormula expected = new Not(new ThereExists(inner));
        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());

        // include actions
        Set<String> actions = new HashSet<>();
        actions.add("a");
        actions.add("b");
        always = new Always(ap, actions);
        formula = new ForAll(always);

        inner = new Until(new BoolProp(true), ap, new HashSet<String>(), actions);
        expected = new Not(new ThereExists(inner));
        enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertForAllEventually() {
        ENFConverter converter = new ENFConverter();

        // empty actions
        AtomicProp ap = new AtomicProp("p");
        Eventually eventually = new Eventually(ap, new HashSet<String>(), new HashSet<String>());
        StateFormula formula = new ForAll(eventually);

        StateFormula expected = new Not(new ThereExists(new Always(new Not(ap), new HashSet<String>())));
        StateFormula enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());

        // include actions
        Set<String> actions = new HashSet<>();
        actions.add("a");
        actions.add("b");
        eventually = new Eventually(ap, new HashSet<String>(), actions);
        formula = new ForAll(eventually);

        expected = new Not(new ThereExists(new Always(new Not(ap), actions)));
        enf = converter.convertToENF(formula);

        assert enf.toString().equals(expected.toString());
    }

    @Test
    public void testConvertThereExistsNext() {
        ENFConverter converter = new ENFConverter();

        AtomicProp ap = new AtomicProp("p");
        StateFormula formula = new ThereExists(new Next(ap, new HashSet<String>()));
        assert converter.convertToENF(formula).toString().equals(formula.toString());

        // include actions
        Set<String> actions = new HashSet<>();
        actions.add("a");
        actions.add("b");
        formula = new ThereExists(new Next(ap, actions));
        assert converter.convertToENF(formula).toString().equals(formula.toString());
    }

    @Test
    public void testConvertThereExistsAlways() {
        ENFConverter converter = new ENFConverter();

        AtomicProp ap = new AtomicProp("p");
        StateFormula formula = new ThereExists(new Always(ap, new HashSet<String>()));
        assert converter.convertToENF(formula).toString().equals(formula.toString());

        // include actions
        Set<String> actions = new HashSet<>();
        actions.add("a");
        actions.add("b");
        formula = new ThereExists(new Always(ap, actions));
        assert converter.convertToENF(formula).toString().equals(formula.toString());
    }

    @Test
    public void testConvertThereExistsEventually() {
        ENFConverter converter = new ENFConverter();

        AtomicProp ap = new AtomicProp("p");
        StateFormula formula = new ThereExists(new Eventually(ap, new HashSet<String>(), new HashSet<String>()));
        StateFormula expected = new ThereExists(new Until(new BoolProp(true), ap, new HashSet<String>(), new HashSet<String>()));
        assert converter.convertToENF(formula).toString().equals(expected.toString());

        // include actions
        Set<String> preActions = new HashSet<>();
        preActions.add("a");
        preActions.add("b");
        Set<String> postActions = new HashSet<>();
        postActions.add("c");
        postActions.add("d");
        formula = new ThereExists(new Eventually(ap, preActions, postActions));
        expected = new ThereExists(new Until(new BoolProp(true), ap, preActions, postActions));
        assert converter.convertToENF(formula).toString().equals(expected.toString());
    }

    @Test
    public void testConvertThereExistsUntil() {
        ENFConverter converter = new ENFConverter();

        AtomicProp p = new AtomicProp("p");
        AtomicProp q = new AtomicProp("q");
        StateFormula formula = new ThereExists(new Until(new BoolProp(true), p, new HashSet<String>(), new HashSet<String>()));
        assert converter.convertToENF(formula).toString().equals(formula.toString());

        Set<String> preActions = new HashSet<>();
        preActions.add("a");
        preActions.add("b");
        Set<String> postActions = new HashSet<>();
        postActions.add("c");
        postActions.add("d");
        formula = new ThereExists(new Until(new BoolProp(true), p, preActions, postActions));
        assert converter.convertToENF(formula).toString().equals(formula.toString());
    }

}
