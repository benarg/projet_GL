package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.SUB;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class Minus extends AbstractOpArith {
    public Minus(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
    	super.codeGenExp(compiler);
    	compiler.addInstruction(new SUB(Register.getR(3),Register.getR(2)));
    	compiler.addInstruction(new PUSH(Register.getR(2)));
        if( (this.getType().isFloat()) && 
            !(compiler.getCompilerOptions().getNoCheck()) ) {
            compiler.addInstruction(new BOV(new Label("overflow_error")));
        }
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        super.byteGenExp(compiler,mw);
        if(this.getLeftOperand().getType().isInt()) {
            mw.visitInsn(Opcodes.ISUB);
        }
        else {
            mw.visitInsn(Opcodes.FSUB);
        }
    }

    @Override
    protected String getOperatorName() {
        return "-";
    }
    
}
