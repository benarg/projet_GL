package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable;

/**
 *
 * @author Ensimag
 * @date 01/01/2017
 */
public class StringType extends Type {

    public StringType(SymbolTable.Symbol name) {
        super(name);
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean sameType(Type otherType) {
        return otherType.isString();
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public String getByteDescriptor() {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
