package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class Initialization extends AbstractInitialization {

    public AbstractExpr getExpression() {
        return expression;
    }

    private AbstractExpr expression;

    public void setExpression(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    public Initialization(AbstractExpr expression) {
        Validate.notNull(expression);
        this.expression = expression;
    }

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError { 

        this.setExpression(this.getExpression().verifyRValue(compiler, localEnv, currentClass, t));
    }

    @Override
    protected void codeGenInitialization(DecacCompiler compiler, DAddr operand) {
        this.expression.codeGenExp(compiler);
        compiler.setIncrementeVariableTemporaires(-1);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new STORE(Register.getR(2), operand));
    }

    @Override
    protected void byteGenInitialization(DecacCompiler compiler, int id, MethodVisitor mv) {
        this.expression.byteGenExp(compiler, mv);
        Type typeVar = this.getExpression().getType();
        if (typeVar.isFloat()) {
            mv.visitVarInsn(Opcodes.FSTORE,id); 
        } else if (typeVar.isClassOrNull()) {
            mv.visitVarInsn(Opcodes.ASTORE,id);
        } else {
            mv.visitVarInsn(Opcodes.ISTORE,id);
        }
    }

    protected void byteGenInitField(AbstractIdentifier fieldName, MethodVisitor mw) {
        // we load 'this'
        mw.visitVarInsn(Opcodes.ALOAD,0);
        this.expression.byteGenExp(null, mw);
        String className = fieldName.getFieldDefinition().getContainingClass().getType().getByteDescriptor();
        String name = fieldName.getName().toString();
        String signature = fieldName.getFieldDefinition().getType().getByteDescriptor();
            if (fieldName.getFieldDefinition().getType().isClass())
                signature = "L" + signature + ";";
        mw.visitFieldInsn(Opcodes.PUTFIELD, className, name, signature);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print(" = ");
        expression.decompile(s);
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        expression.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        expression.prettyPrint(s, prefix, true);
    }
}
