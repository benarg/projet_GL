package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import bytecode.BytegenError;

/**
 * Entry point for contextual verifications and code generation from outside the package.
 * 
 * @author gl56
 * @date 01/01/2017
 *
 */
public abstract class AbstractProgram extends Tree {
    public abstract void verifyProgram(DecacCompiler compiler) throws ContextualError;
    public abstract void codeGenProgram(DecacCompiler compiler) ;
    public abstract void byteGenProgram(DecacCompiler compiler) throws BytegenError;

}
