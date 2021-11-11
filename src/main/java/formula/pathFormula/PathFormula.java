package formula.pathFormula;

import formula.ENFConverter;
import formula.stateFormula.StateFormula;

public abstract class PathFormula {
    public abstract void writeToBuffer(StringBuilder buffer);

    // visitor pattern : delegates to the method of the converter (visitor)
    public abstract StateFormula convertForAll(ENFConverter converter);
    public abstract StateFormula convertThereExists(ENFConverter converter);
}
