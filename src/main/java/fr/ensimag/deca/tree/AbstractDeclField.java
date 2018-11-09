package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * Variable declaration
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractDeclField extends Tree {
    
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
    protected abstract void verifyDeclField(DecacCompiler compiler,
            ClassDefinition superClass, ClassDefinition currentClass)
            throws ContextualError;

    protected abstract void verifyDeclField_pass3(DecacCompiler comiler, 
            EnvironmentExp envExp, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Generate code to initialize fields of an object
     */
    protected abstract void codeGenInitialization(DecacCompiler compiler, int offsetField);
    protected abstract void codeGenNoInitialization(DecacCompiler compiler);
    
    protected abstract void byteGenDeclField(ClassWriter currentCW, MethodVisitor initMW);

}
