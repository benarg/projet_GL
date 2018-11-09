package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.TSTO;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class ListDeclField extends TreeList<AbstractDeclField> {

    /**
     * Implements non-terminal "list_field" of [SyntaxeContextuelle] in pass 2 and 3
     * @param compiler contains "env_types" attribute
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass 
     *          corresponds to "class" attribute (null in the main bloc).
     * @param returnType
     *          corresponds to "return" attribute (void in the main bloc).
     */    
    public void verifyListField(DecacCompiler compiler, ClassDefinition superClass,
            ClassDefinition currentClass)
        throws ContextualError {

            for (AbstractDeclField f : getList()) {
                f.verifyDeclField(compiler, superClass, currentClass);
            }

    }

    public void verifyListField_pass3(DecacCompiler compiler, EnvironmentExp envExp, 
            ClassDefinition currentClass) throws ContextualError {

                for (AbstractDeclField f : getList()) {
                    f.verifyDeclField_pass3(compiler, envExp, currentClass);
                }

    }
	
    public void codeGenListField(DecacCompiler compiler, ClassDefinition currentClass, int numFieldsParents) {
        int offsetField;
        Label initFields = new Label("init." + currentClass.getType().getName() );
        compiler.addLabel(initFields);
        compiler.addInstruction(new LOAD(new RegisterOffset(-2,Register.LB), Register.getR(1)));
        if (numFieldsParents != 0) {
            compiler.addInstruction(new TSTO(3)); // VALEUR 3 ARBITRAIRE: A MODIFIER !!!
            compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
            offsetField = numFieldsParents;
            for (AbstractDeclField f : getList()) {
                offsetField++;
                f.codeGenNoInitialization(compiler);
                compiler.addInstruction(new STORE(Register.getR(0), new RegisterOffset(offsetField , Register.R1)));
            }

            compiler.addInstruction(new PUSH(Register.getR(1)));
            compiler.addInstruction(new BSR(new Label("init." + currentClass.getSuperClass().getType().getName())));
            compiler.addInstruction(new SUBSP(1));
            
            compiler.addInstruction(new LOAD(new RegisterOffset(-2,Register.LB), Register.getR(1)));
        }
        offsetField = numFieldsParents;
        for (AbstractDeclField f : getList()) {
            f.codeGenInitialization(compiler, ++offsetField);
        }
        compiler.addInstruction(new RTS());
    }

    public void byteGenListField(ClassWriter currentCW, MethodVisitor initMW) {
        for (AbstractDeclField f : getList()) {
            f.byteGenDeclField(currentCW, initMW);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclField a : getList()) {
            a.decompile(s);
            s.println();
        }
    }
}
