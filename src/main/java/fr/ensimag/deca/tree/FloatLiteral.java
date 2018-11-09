package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;

/**
 * Single precision, floating-point literal
 *
 * @author gl56
 * @date 01/01/2017
 */
public class FloatLiteral extends AbstractExpr {

    public float getValue() {
        return value;
    }

    private float value;

    public FloatLiteral(float value) {
        Validate.isTrue(!Float.isInfinite(value),
                "literal values cannot be infinite");
        Validate.isTrue(!Float.isNaN(value),
                "literal values cannot be NaN");
        this.value = value;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Symbol floatSymbol = compiler.getSymbolTable().create("float");
        FloatType thisType = new FloatType(floatSymbol);
        setType(thisType);
            
        return thisType;       
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(new ImmediateFloat(this.value),Register.getR(1)));
    }

    @Override
    protected void byteGenPrint(DecacCompiler compiler, MethodVisitor mw) {
        // pushes the float value
        mw.visitLdcInsn(this.value);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(new ImmediateFloat(this.value), Register.getR(2)));
        compiler.setIncrementeVariableTemporaires(1);
        if (compiler.getVariableTemporaires() > compiler.getVariableTemporaireMax())
            compiler.setVariableTemporaireMax(compiler.getVariableTemporaires());
        compiler.addInstruction(new PUSH(Register.getR(2)));

    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        // pushes the float value
        mw.visitLdcInsn(this.value);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(java.lang.Float.toHexString(value));
    }

    @Override
    String prettyPrintNode() {
        return "Float (" + getValue() + ")";
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
