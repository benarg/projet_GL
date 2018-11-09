package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class And extends AbstractOpBool {

    private static int nbAnd = 1;

    public And(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        
        Label lFalse = new Label("and_false_" + nbAnd);
        Label lEnd   = new Label("and_end_" + nbAnd);
        nbAnd++;

        // We evaluate the left operand
        AbstractExpr opGauche = this.getLeftOperand();
        evalueOperand(compiler, opGauche, lFalse);

        // We then evaluate the right operand, if necessary (opGauche == true)
        AbstractExpr opDroite = this.getRightOperand();
        evalueOperand(compiler, opDroite, lFalse);

        compiler.setIncrementeVariableTemporaires(-1);

        // If both operands are true => we go straight at the end
        compiler.addInstruction(new BRA(lEnd));

        // and_false_nbAnd:
        compiler.addLabel(lFalse);
        compiler.addInstruction(new LOAD(0, Register.getR(2)));

        // and_end_nbAnd:
        compiler.addLabel(lEnd);
        compiler.addInstruction(new PUSH(Register.getR(2)));
        
    }

    /**
     * we evaluate the operand to see if it's false, if yes we branch on the
     * false label
     * 
     * @param compiler [the compiler]
     * @param op       [operand]
     * @param l        [false label]
     */
    private static void evalueOperand(DecacCompiler compiler, AbstractExpr op,
                                      Label l) {
        op.codeGenExp(compiler);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new CMP(0, Register.getR(2)));
        compiler.addInstruction(new BEQ(l));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {

        org.objectweb.asm.Label lFalse = new org.objectweb.asm.Label();
        org.objectweb.asm.Label lEnd  = new org.objectweb.asm.Label();

        // We evaluate the left operand
        AbstractExpr opGauche = this.getLeftOperand();
        evalueOperand(compiler, mw, opGauche, lFalse);

        // We then evaluate the right operand, if necessary (opGauche == true)
        AbstractExpr opDroite = this.getRightOperand();
        evalueOperand(compiler, mw, opDroite, lFalse);

        // If both operands are true => we go straight at the end
        mw.visitInsn(Opcodes.ICONST_1);
        mw.visitJumpInsn(Opcodes.GOTO, lEnd);

        // and_false:
        mw.visitLabel(lFalse);
        mw.visitInsn(Opcodes.ICONST_0);

        // and_end:
        mw.visitLabel(lEnd);

    }

    /**
     * Same purpose as the function of the same name above 
     *
     * @param compiler [the compiler]
     * @param mw       [the Method Visitor]
     * @param op       [operand]
     * @param l        [false label]
     */
    private static void evalueOperand(DecacCompiler compiler, MethodVisitor mw,
                                      AbstractExpr op, org.objectweb.asm.Label l) {
        op.byteGenExp(compiler, mw);
        mw.visitJumpInsn(Opcodes.IFEQ,l);
    }

    @Override
    protected String getOperatorName() {
        return "&&";
    }

}
