package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class While extends AbstractInst {
    private AbstractExpr condition;
    private ListInst body;

    public AbstractExpr getCondition() {
        return condition;
    }

    public ListInst getBody() {
        return body;
    }

    public While(AbstractExpr condition, ListInst body) {
        Validate.notNull(condition);
        Validate.notNull(body);
        this.condition = condition;
        this.body = body;
    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        Label whileLabel = new Label("while_" + this.getLocation().getLine());
        Label endLabel = new Label("endWhile_" + this.getLocation().getLine());
        compiler.addLabel(whileLabel);
        this.getCondition().codeGenExp(compiler);
        compiler.setIncrementeVariableTemporaires(-1);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new CMP(0, Register.getR(2)));
        compiler.addInstruction(new BEQ(endLabel));  
        this.getBody().codeGenListInst(compiler);
        compiler.addInstruction(new BRA(whileLabel));
        compiler.addLabel(endLabel);
    }

    @Override
    protected void byteGenInst(DecacCompiler compiler, MethodVisitor mw) {
        // Create labels for jumps in Opcodes operators
        org.objectweb.asm.Label whileLabel = new org.objectweb.asm.Label();
        org.objectweb.asm.Label endLabel = new org.objectweb.asm.Label();

        // Add while label
        mw.visitLabel(whileLabel);
        this.getCondition().byteGenExp(compiler, mw);

        // Comparison with 0 (false), if 0 go to end label
        mw.visitJumpInsn(Opcodes.IFEQ, endLabel);
        this.getBody().byteGenListInst(compiler, mw);

        // Restart: goto the while label
        mw.visitJumpInsn(Opcodes.GOTO, whileLabel);

        // Add end label
        mw.visitLabel(endLabel);
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
                this.getCondition().verifyCondition(compiler, localEnv, currentClass);
                this.getBody().verifyListInst(compiler, localEnv, currentClass, returnType);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("while (");
        getCondition().decompile(s);
        s.println(") {");
        s.indent();
        getBody().decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        getCondition().iter(f);
        getBody().iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        getCondition().prettyPrint(s, prefix, false);
        getBody().prettyPrint(s, prefix, true);
    }

}
