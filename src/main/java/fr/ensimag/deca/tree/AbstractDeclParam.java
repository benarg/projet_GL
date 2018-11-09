package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.ParamDefinition;
import java.io.PrintStream;

/**
 *
 * @author gl56
 */
public abstract class AbstractDeclParam extends Tree{
    

    protected abstract Type verifyDeclParam(DecacCompiler compiler, int stackIndex)
            throws ContextualError;

    protected abstract ParamDefinition verifyDeclParam_pass3(DecacCompiler compiler)
            throws ContextualError;

    protected abstract AbstractIdentifier getName();

    protected abstract String getByteDesc(int numDeclared);
    
}
