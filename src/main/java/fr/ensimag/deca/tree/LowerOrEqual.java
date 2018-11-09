package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.SLE;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.Register;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Label; 

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class LowerOrEqual extends AbstractOpIneq {
    public LowerOrEqual(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
    	super.codeGenExp(compiler);
    	compiler.addInstruction(new CMP(Register.getR(3),Register.getR(2)));
    	compiler.addInstruction(new SLE(Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {
        super.byteGenExp(compiler,mv);

        // Create labels for jumps in comparison operators
        Label labelTrue = new Label();
        Label labelEnd = new Label();

        // Comparison with 0  
        mv.visitJumpInsn(Opcodes.IFLE, labelTrue);

        // Put 0 on stack for false and goto the end label
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.GOTO, labelEnd);

        // Put 1 on stack for true and finish
        mv.visitLabel(labelTrue);
        mv.visitInsn(Opcodes.ICONST_1);

        // Add end label
        mv.visitLabel(labelEnd);
    }

    @Override
    protected String getOperatorName() {
        return "<=";
    }

}
