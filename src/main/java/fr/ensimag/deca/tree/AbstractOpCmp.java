package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractOpCmp extends AbstractBinaryExpr {

    public AbstractOpCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {

        super.byteGenExp(compiler,mv);

        if(this.getLeftOperand().getType().isFloat()) {
            mv.visitInsn(Opcodes.FCMPG); 
        } else {
            mv.visitInsn(Opcodes.ISUB);
        }
        
    }

}
