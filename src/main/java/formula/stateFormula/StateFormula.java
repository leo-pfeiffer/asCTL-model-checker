package formula.stateFormula;

import formula.ENFConverter;

public abstract class StateFormula {
    public abstract void writeToBuffer(StringBuilder buffer);

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        writeToBuffer(buffer);
        return buffer.toString();
    }

    // visitor pattern : delegates to the method of the converter (visitor)
    public abstract StateFormula convertToENF(ENFConverter converter);
}
