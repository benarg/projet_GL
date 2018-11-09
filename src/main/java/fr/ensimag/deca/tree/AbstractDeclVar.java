package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import org.objectweb.asm.MethodVisitor;

/**
 * Variable declaration
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractDeclVar extends Tree {
    
    /**
     * Implements non-terminal "decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to the "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the synthetized attribute
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     */    
    protected abstract void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Generate code if necessary to declare variable
     *
     * @param compiler
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     * @param numDeclared index of the declared variable
     */
    protected abstract void codeGenDeclVar(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared);
    protected abstract void byteGenDeclVar(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared, MethodVisitor mv);

}
