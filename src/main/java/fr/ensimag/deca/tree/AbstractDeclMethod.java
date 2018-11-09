package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Label;

import org.objectweb.asm.ClassWriter;

/**
 * Variable declaration
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractDeclMethod extends Tree {
    
    /**
     * Implements non-terminal "decl_method" of [SyntaxeContextuelle] in pass 1, 2 and 3
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
    protected abstract void verifyDeclMethod(DecacCompiler compiler,
            ClassDefinition superClass, ClassDefinition currentClass)
            throws ContextualError;


    protected abstract void verifyDeclMethod_pass3(DecacCompiler compiler, 
            EnvironmentExp envExp, ClassDefinition currentClass) 
            throws ContextualError;
            

    /**
     * Generate code if necessary to declare variable
     *
     * @param compiler
     * @param currentClass 
     *          corresponds to the "class" attribute (null in the main bloc).
     * @param numDeclared index of the declared variable
     */
    protected abstract void codeGenDeclMethod(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared);
    protected abstract AbstractIdentifier getMethodName();
    //protected abstract void codeGenMethodTable(DecacCompiler compiler, ClassDefinition currentClass, Label label);

    protected abstract void byteGenDeclMethod(DecacCompiler compiler, ClassDefinition currentClass, ClassWriter cw);
}
