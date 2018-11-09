package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl56
 */
public abstract class AbstractMethodBody extends Tree {
    
    protected abstract ListDeclVar getDeclVars();
    protected abstract ListInst getInsts();

    protected abstract void verifyMethodBody(DecacCompiler compiler,
                                             EnvironmentExp envExp,
                                             EnvironmentExp envExpParams,
                                             ClassDefinition currentClass,
                                             Type tType) 
            throws ContextualError;

    protected abstract void codeGenInst(DecacCompiler compiler,
                                        ClassDefinition currentClass,
                                        int numDeclared,
                                        AbstractIdentifier methodName);

    protected abstract void byteGenInst(DecacCompiler compiler, ClassDefinition currentClass, 
            MethodVisitor mw, int numParams);
}
