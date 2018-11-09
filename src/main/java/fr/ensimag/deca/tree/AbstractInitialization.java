package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.DAddr;
import org.objectweb.asm.MethodVisitor;

/**
 * Initialization (of variable, field, ...)
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractInitialization extends Tree {
    
    /**
     * Implements non-terminal "initialization" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param t corresponds to the "type" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */
    protected abstract void verifyInitialization(DecacCompiler compiler,
            Type t, EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Generate code if necessary to declare variable
     *
     * @param compiler
     * @param operand operand where to initialize the expression
     */
    protected abstract void codeGenInitialization(DecacCompiler compiler, DAddr operand);
    protected abstract void byteGenInitialization(DecacCompiler compiler, int id, MethodVisitor mv);

    protected abstract void byteGenInitField(AbstractIdentifier fieldName, MethodVisitor mw);

}
