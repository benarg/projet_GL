package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import org.objectweb.asm.MethodVisitor;


/**
 * List of declarations (e.g. int x; float y,z).
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class ListDeclVar extends TreeList<AbstractDeclVar> {

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclVar a : getList()) {
            a.decompile(s);
            s.println();
        }
    }

    /**
     * Implements non-terminal "list_decl_var" of [SyntaxeContextuelle] in pass 3
     * @param compiler contains the "env_types" attribute
     * @param localEnv 
     *   its "parentEnvironment" corresponds to "env_exp_sup" attribute
     *   in precondition, its "current" dictionary corresponds to 
     *      the "env_exp" attribute
     *   in postcondition, its "current" dictionary corresponds to 
     *      the "env_exp_r" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */    
    void verifyListDeclVariable(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        for (AbstractDeclVar a : getList()) {
            a.verifyDeclVar(compiler, localEnv, currentClass);
        }
    }

    /**
     * Generate code if necessary to declare variable
     *
     * @param compiler
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     */
    void codeGenListDeclVar(DecacCompiler compiler, ClassDefinition currentClass) {
        int numDeclared = 0;
        if (currentClass == null)
            numDeclared = compiler.getVariableGlobales();
        for (AbstractDeclVar a : getList()) {
            a.codeGenDeclVar(compiler, currentClass, numDeclared);
            numDeclared++;
        }
    }

    void byteGenListDeclVar(DecacCompiler compiler, ClassDefinition currentClass, MethodVisitor mv,
            int numDeclared){
        
        for (AbstractDeclVar a : getList()) {
            a.byteGenDeclVar(compiler, currentClass, numDeclared, mv);
            numDeclared++;
        }
    }


}
