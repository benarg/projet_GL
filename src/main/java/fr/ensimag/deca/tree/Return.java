package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.POP;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl56
 */
public class Return extends AbstractInst {
    private AbstractExpr expr;
    private Label methodLabel;
    
    public Return(AbstractExpr expr) {
        Validate.notNull(expr);
        this.expr = expr;
    }
    
    public AbstractExpr getExpr() {
        return this.expr;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass, Type returnType) 
    throws ContextualError {

        if (returnType.isVoid()) {
            throw new ContextualError("Method has void returnType (rule 3.24)", this.getLocation());
        }

        methodLabel = new Label("fin." + currentClass.getType() + "." + localEnv.getLabel());

        expr.verifyRValue(compiler, localEnv, currentClass, returnType);

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        expr.codeGenExp(compiler);
        compiler.addInstruction(new POP(Register.getR(0)));
        compiler.addInstruction(new BRA(methodLabel));
    }
    

    @Override
    protected void byteGenInst(DecacCompiler compiler, MethodVisitor mw) {
        expr.byteGenExp(compiler, mw);
        Type type = expr.getType();
        if (type.isFloat()) {
            mw.visitInsn(Opcodes.FRETURN);
        } else if (type.isClass()) {
            mw.visitInsn(Opcodes.ARETURN);
        } else {
            mw.visitInsn(Opcodes.IRETURN);
        }
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("return ");
        expr.decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expr.prettyPrint(s, prefix, true);
       
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        expr.iter(f);
    }
}
