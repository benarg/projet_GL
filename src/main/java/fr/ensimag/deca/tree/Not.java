package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SNE;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes; 

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Not extends AbstractUnaryExpr {

    public Not(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type tOp = this.getOperand().verifyExpr(compiler, localEnv, currentClass);
        if (!(tOp.isBoolean())) {        
            throw new ContextualError
                ("Expected boolean got " + tOp + " (rule 3.63)", this.getOperand().getLocation());
        }
        this.setType(tOp);
        return tOp;
    }

    @Override
    public void codeGenExp(DecacCompiler compiler) {
        super.codeGenExp(compiler);
        compiler.addInstruction(new CMP(1, Register.getR(2)));
        compiler.addInstruction(new SNE(Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {
        super.byteGenExp(compiler,mv);

        // Push 1 to the stack and add it to the concerning value
        mv.visitIntInsn(Opcodes.SIPUSH, 1);
        mv.visitInsn(Opcodes.IADD);

        // Push 2 to the stack and substract the value from last operation
        mv.visitIntInsn(Opcodes.SIPUSH, 2);
        mv.visitInsn(Opcodes.IREM);
        
    } 
        
    @Override
    protected String getOperatorName() {
        return "!";
    }
}
