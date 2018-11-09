package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.OPP;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class UnaryMinus extends AbstractUnaryExpr {

    public UnaryMinus(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
                
                AbstractExpr operand = this.getOperand();
                Type operandType = operand.verifyExpr(compiler, localEnv, currentClass);

                if (!(operandType.isFloat() || operandType.isInt())) {
                    throw new ContextualError("Expected int or float got " + operandType +
                     " (rule 3.62)", this.getOperand().getLocation());
                }
                
                this.setType(operandType);
                return operandType;
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        super.codeGenExp(compiler);
        compiler.addInstruction(new OPP(Register.getR(2), Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        if(this.getOperand().getType().isInt()) {
            mw.visitInsn(Opcodes.ICONST_0);
            super.byteGenExp(compiler, mw);
            mw.visitInsn(Opcodes.ISUB);
        }
        else {
            mw.visitInsn(Opcodes.FCONST_0);
            super.byteGenExp(compiler, mw);
            mw.visitInsn(Opcodes.FSUB);
        }
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }

}
