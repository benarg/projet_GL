package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.Register;
import java.io.PrintStream;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class BooleanLiteral extends AbstractExpr {

    private boolean value;

    public BooleanLiteral(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
	    
        Symbol booleanSymbol = compiler.getSymbolTable().create("boolean");
        BooleanType thisType = new BooleanType(booleanSymbol);
        setType(thisType);
        
        return thisType;
	
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {

        if (this.getValue() == true) {
            compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.getR(2)));
        } else {
            compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.getR(2)));
        }

        compiler.setIncrementeVariableTemporaires(1);

        if (compiler.getVariableTemporaires() > compiler.getVariableTemporaireMax()) {
            compiler.setVariableTemporaireMax(compiler.getVariableTemporaires());
        }

        compiler.addInstruction(new PUSH(Register.getR(2)));

    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {

        if (this.getValue() == true) {
            mv.visitInsn(Opcodes.ICONST_1);
        } else {
            mv.visitInsn(Opcodes.ICONST_0);
        }
        
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Boolean.toString(getValue()));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

    @Override
    String prettyPrintNode() {
        return "BooleanLiteral (" + getValue() + ")";
    }

}
