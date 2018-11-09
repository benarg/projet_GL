package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.QUO;
import fr.ensimag.ima.pseudocode.instructions.DIV;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Divide extends AbstractOpArith {
    public Divide(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
    	super.codeGenExp(compiler);
        if (getLeftOperand().getType().isInt())
    	   compiler.addInstruction(new QUO(Register.getR(3),Register.getR(2)));
        else
           compiler.addInstruction(new DIV(Register.getR(3),Register.getR(2)));
    	compiler.addInstruction(new PUSH(Register.getR(2)));
        if(!compiler.getCompilerOptions().getNoCheck()) {
            compiler.addInstruction(new BOV(new Label("divide_by_zero")));
            if(this.getType().isFloat()) {
                compiler.addInstruction(new BOV(new Label("overflow_error")));
            }
        }
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        super.byteGenExp(compiler,mw);
        if(this.getLeftOperand().getType().isInt()) {
            mw.visitInsn(Opcodes.IDIV);
        }
        else {
            mw.visitInsn(Opcodes.FDIV);
        }
    }

    @Override
    protected String getOperatorName() {
        return "/";
    }

}
