package formula;

import model.State;
import java.util.Set;

// visitor pattern
// https://www.infoworld.com/article/2077602/java-tip-98--reflect-on-the-visitor-design-pattern.html

public interface Visitable {
    public Set<State> accept(Visitor visitor, Set<State> states);
}