package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.instructions.REM;
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
public class Modulo extends AbstractOpArith {

    public Modulo(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type tLeftOp = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRightOp = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);
       
        if (!(tLeftOp.isInt())) {        
            throw new ContextualError
                ("Expected int got " + tLeftOp + " (rule 3.51)", this.getLeftOperand().getLocation());
        }
        if (!(tRightOp.isInt())) {        
            throw new ContextualError
                ("Expected int got " + tRightOp + " (rule 3.51)", this.getRightOperand().getLocation());
        }
        this.setType(tLeftOp);
        return tLeftOp;
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        super.codeGenExp(compiler);
        compiler.addInstruction(new REM(Register.getR(3),Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(2)));
        if(!compiler.getCompilerOptions().getNoCheck()){
            compiler.addInstruction(new BOV(new Label("divide_by_zero")));
        }
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        super.byteGenExp(compiler,mw);
        mw.visitInsn(Opcodes.IREM);
    }

    @Override
    protected String getOperatorName() {
        return "%";
    }

}
