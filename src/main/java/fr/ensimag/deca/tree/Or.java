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
public class Or extends AbstractOpBool {

    private static int nbOr = 1;

    public Or(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        
        Label lTrue = new Label("or_true_" + nbOr);
        Label lEnd  = new Label("or_end_" + nbOr);
        nbOr++;

        // On evalue l'operande gauche
        AbstractExpr opGauche = this.getLeftOperand();
        evalueOperand(compiler, opGauche, lTrue);

        // On evalue ensuite l'operande droit, si besoin (opGauche == false)
        AbstractExpr opDroite = this.getRightOperand();
        evalueOperand(compiler, opDroite, lTrue);

        compiler.setIncrementeVariableTemporaires(-1);

        // Si les deux operandes sont a false => on va directement a la fin
        compiler.addInstruction(new BRA(lEnd));

        // or_true_nbOr:
        compiler.addLabel(lTrue);
        compiler.addInstruction(new LOAD(1, Register.getR(2)));

        // or_end_nbOr:
        compiler.addLabel(lEnd);
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    private static void evalueOperand(DecacCompiler compiler, AbstractExpr op,
                                      Label l) {
        op.codeGenExp(compiler);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new CMP(1, Register.getR(2)));
        compiler.addInstruction(new BEQ(l));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        org.objectweb.asm.Label lTrue = new org.objectweb.asm.Label();
        org.objectweb.asm.Label lEnd  = new org.objectweb.asm.Label();

        // On evalue l'operande gauche
        AbstractExpr opGauche = this.getLeftOperand();
        evalueOperand(compiler, mw, opGauche, lTrue);

        // On evalue ensuite l'operande droit, si besoin (opGauche == false)
        AbstractExpr opDroite = this.getRightOperand();
        evalueOperand(compiler, mw, opDroite, lTrue);

        // Si les deux operandes sont a false => on va directement a la fin
        mw.visitInsn(Opcodes.ICONST_0);
        mw.visitJumpInsn(Opcodes.GOTO, lEnd);

        // or_true:
        mw.visitLabel(lTrue);
        mw.visitInsn(Opcodes.ICONST_1);

        // or_end:
        mw.visitLabel(lEnd);
    }

    private static void evalueOperand(DecacCompiler compiler, MethodVisitor mw,
                                      AbstractExpr op, org.objectweb.asm.Label l) {
        op.byteGenExp(compiler, mw);
        mw.visitJumpInsn(Opcodes.IFGT,l);
    }

    @Override
    protected String getOperatorName() {
        return "||";
    }


}
