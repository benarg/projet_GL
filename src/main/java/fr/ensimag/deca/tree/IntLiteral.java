package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.Register;
import java.io.PrintStream;
import org.objectweb.asm.MethodVisitor;

/**
 * Integer literal
 *
 * @author gl56
 * @date 01/01/2017
 */
public class IntLiteral extends AbstractExpr {
    public int getValue() {
        return value;
    }

    private int value;

    public IntLiteral(int value) {
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        
        Symbol intSymbol = compiler.getSymbolTable().create("int");
        IntType thisType = new IntType(intSymbol);
        setType(thisType);
            
        return thisType;
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(this.value, Register.getR(1)));
    }

    @Override
    protected void byteGenPrint(DecacCompiler compiler, MethodVisitor mw) {
        // pushes the int value
        mw.visitLdcInsn(this.value);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(new ImmediateInteger(this.value), Register.getR(2)));
        compiler.setIncrementeVariableTemporaires(1);
        if (compiler.getVariableTemporaires() > compiler.getVariableTemporaireMax())
            compiler.setVariableTemporaireMax(compiler.getVariableTemporaires());
        compiler.addInstruction(new PUSH(Register.getR(2)));

    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        // pushes the int value
        mw.visitLdcInsn(this.value);
    }

    @Override
    String prettyPrintNode() {
        return "Int (" + getValue() + ")";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(Integer.toString(value));
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}
