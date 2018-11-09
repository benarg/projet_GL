package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;

import org.objectweb.asm.ClassWriter;

/**
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class ListDeclMethod extends TreeList<AbstractDeclMethod> {

    /**
     * Implements non-terminal "list_method" of [SyntaxeContextuelle] in pass 2 and 3
     * @param compiler contains "env_types" attribute
     * @param superClass
     * @param currentClass
     */    
    public void verifyListMethod(DecacCompiler compiler,
            ClassDefinition superClass, ClassDefinition currentClass) 
            throws ContextualError {

                for (AbstractDeclMethod m : getList()) {
                    m.verifyDeclMethod(compiler, superClass, currentClass);
                }
	
    }
    
     public void verifyListMethod_pass3(DecacCompiler compiler, 
        EnvironmentExp envExp, ClassDefinition classDef) 
            throws ContextualError {

            for (AbstractDeclMethod m : getList()) {
                m.verifyDeclMethod_pass3(compiler, envExp, classDef);
            }

        }


   public void codeGenMethodsTable(DecacCompiler compiler, ClassDefinition currentClass) {

        LabelOperand labelOperandsMethodsTab[] = new LabelOperand[currentClass.getNumberOfMethods()];

        int indexTableMeth = compiler.getVariableGlobales();
        compiler.addVariableGlobales(currentClass.getNumberOfMethods());

        int index = 0;
        for (LabelOperand lOperand : currentClass.getSuperClass().getLabelOperandsMethodsTable()) {
            compiler.addInstruction(new LOAD(lOperand, Register.getR(0)));
            compiler.addInstruction(new STORE(Register.getR(0), 
                        new RegisterOffset(indexTableMeth + index + 1, Register.GB)));
            labelOperandsMethodsTab[index] = lOperand;
            index++;
        }

        for (AbstractDeclMethod m : getList()) {
            Label methodsLabel = new Label("code." + currentClass.getType().getName() + "." + m.getMethodName().getName());
            m.getMethodName().getMethodDefinition().setLabel(methodsLabel);
            LabelOperand lOperand = new LabelOperand(methodsLabel);
            compiler.addInstruction(new LOAD(lOperand, Register.getR(0)));
            compiler.addInstruction(new STORE(Register.getR(0), 
                        new RegisterOffset(indexTableMeth + m.getMethodName().getMethodDefinition().getIndex() + 1, Register.GB)));

            labelOperandsMethodsTab[m.getMethodName().getMethodDefinition().getIndex()] = lOperand;
        }
        
        currentClass.setLabelOperandsMethodsTable(labelOperandsMethodsTab);
        
       
    }
   
   
    public void codeGenListMethod(DecacCompiler compiler, ClassDefinition currentClass) {
        for (AbstractDeclMethod m : getList()) {
            m.codeGenDeclMethod(compiler, currentClass, m.getMethodName().getMethodDefinition().getIndex());
        }
    }

    public void byteGenListMethods(DecacCompiler compiler, ClassDefinition currentClass, 
            ClassWriter cw) {
        for (AbstractDeclMethod m : getList()) {
            m.byteGenDeclMethod(compiler, currentClass, cw);
        }
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclMethod a : getList()) {
            a.decompile(s);
        }
    }
}
