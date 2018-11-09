package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;
import org.apache.log4j.Logger;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class ListDeclClass extends TreeList<AbstractDeclClass> {
    private static final Logger LOG = Logger.getLogger(ListDeclClass.class);
    
    @Override
    public void decompile(IndentPrintStream s) {
        for (AbstractDeclClass c : getList()) {
            c.decompile(s);
            s.println();
        }
    }

    /**
     * Pass 1 of [SyntaxeContextuelle]
     */
    void verifyListClass(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify listClass: start");
        
        for (AbstractDeclClass c : getList()) {
            c.verifyClass(compiler);
        }

        LOG.debug("verify listClass: end");
    }

    /**
     * Pass 2 of [SyntaxeContextuelle]
     */
    public void verifyListClassMembers(DecacCompiler compiler) throws ContextualError {
        
        for (AbstractDeclClass c : getList()) {
            c.verifyClassMembers(compiler);
        }

    }

    
    /**
     * Pass 3 of [SyntaxeContextuelle]
     */
    public void verifyListClassBody(DecacCompiler compiler) throws ContextualError {
        
        for (AbstractDeclClass c : getList()) {
            c.verifyClassBody(compiler);
        }
    }

    /**
     * Writing the code for the classes
     */
    public void codeGenListDeclClass(DecacCompiler compiler) {
        codeGenObjectClass(compiler);
        for (AbstractDeclClass c : getList()) {
            c.codeGenDeclClass(compiler);
        }
    }

    private void codeGenObjectClass(DecacCompiler compiler) {
        Symbol objectSymbol = compiler.getSymbolTable().create("Object");
        TypeDefinition objectTypeDef = compiler.getEnvType().get(objectSymbol);
        if (!(objectTypeDef instanceof ClassDefinition))
            throw new DecacInternalError("Le typeDef de Object devrait etre un classDef");
        ClassDefinition objectClassDef = (ClassDefinition) objectTypeDef;
        objectClassDef.setOperand(new RegisterOffset(1, Register.GB));
        compiler.addInstruction(new LOAD(new NullOperand(), Register.getR(0)));
        compiler.addInstruction(new STORE(Register.getR(0), objectClassDef.getOperand()));
        LabelOperand lOperand = objectClassDef.getLabelOperandsMethodsTable()[0];
        compiler.addInstruction(new LOAD(lOperand, Register.getR(0)));
        compiler.addInstruction(new STORE(Register.getR(0), new RegisterOffset(2, Register.GB)));
        compiler.setVariableGlobales(2);
    }

    /**
     * Writing the codes of each method of each class
     */
    public void codeGenListDeclClassMethods(DecacCompiler compiler) {
        codeGenObjectMethods(compiler);

        for (AbstractDeclClass c : getList()) {
            c.codeGenClassMethods(compiler);
        }

    }

    private void codeGenObjectMethods(DecacCompiler compiler) {
        compiler.addComment("-------Object-------");
        compiler.addLabel(new Label("init.Object"));
        compiler.addInstruction(new RTS());
        compiler.addLabel(new Label("code.Object.equals"));
        compiler.addInstruction(new TSTO(2));
        compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
        compiler.addInstruction(new PUSH(Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(3)));
        compiler.addInstruction(new LOAD(new RegisterOffset(-2,Register.LB), Register.getR(2)));
        compiler.addInstruction(new LOAD(new RegisterOffset(-3,Register.LB), Register.getR(3)));
        compiler.addInstruction(new CMP(Register.getR(3), Register.getR(2)));
        compiler.addInstruction(new SEQ(Register.getR(0)));
        compiler.addComment("Restauration des registres");
        compiler.addInstruction(new POP(Register.getR(3)));
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new RTS());

    }

    public void byteGenListDeclClass(DecacCompiler compiler) {
        for (AbstractDeclClass c : getList()) {
            c.byteGenDeclClass(compiler);
        }
    }

}
