package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.FLOAT;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Conversion of an int into a float. Used for implicit conversions.
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class ConvFloat extends AbstractUnaryExpr {

    public ConvFloat(AbstractExpr operand) {
        super(operand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        throw new DecacInternalError("verifyExpr should never be called on ConvFloat");
    }

    @Override
    public void codeGenExp(DecacCompiler compiler) {
        this.getOperand().codeGenExp(compiler);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new FLOAT(Register.getR(2), Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    @Override
    public void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {
        this.getOperand().byteGenExp(compiler, mv);
        //Opcode I2F allows to convert a Integer into a Float.
        mv.visitInsn(Opcodes.I2F);
    }

    @Override
    protected String getOperatorName() {
        return "/* conv float */";
    }

}
