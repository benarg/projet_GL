package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 */
public class Cast extends AbstractBinaryExpr {

    private boolean checkDynamically = false;
    private static int numDynCheck = 0;
    
    public Cast(AbstractIdentifier leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }
    
    @Override
    protected String getOperatorName() {
        return " cast ";
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("(");
        getLeftOperand().decompile(s);
        s.print(") (");
        getRightOperand().decompile(s);
        s.print(")");
    }
    
    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type tLeftOp = ((AbstractIdentifier) this.getLeftOperand()).verifyType(compiler);
    	Type tRightOp = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!(tRightOp.sameType(tLeftOp))) {

            if (tLeftOp.isClass()) {
                checkDynamically = true;
            } else {

                if (!(tLeftOp.isFloat() || tLeftOp.isInt())) {
                    throw new ContextualError("Expected int or float got " + tLeftOp + 
                        " (rule 3.39)", this.getLeftOperand().getLocation());
                }

                if (!(tRightOp.isFloat() || tRightOp.isInt())) {
                    throw new ContextualError("Expected int or float got " + tRightOp + 
                        " (rule 3.39)", this.getRightOperand().getLocation());
                }

                if (tLeftOp.isFloat()) {
                    this.setRightOperand(new ConvFloat(this.getRightOperand()));
                    this.getRightOperand().setType(tRightOp);
                } else {
                    this.setRightOperand(new ConvInt(this.getRightOperand()));
                    this.getRightOperand().setType(tLeftOp);
                }

            }

        }
  
        this.setType(tLeftOp);
       	return tLeftOp;

    }
    
    @Override
    public void codeGenExp(DecacCompiler compiler) {

        this.getRightOperand().codeGenExp(compiler);

        if (checkDynamically) {

            numDynCheck++;
            Label whileLabel = new Label("Cast_check_" + numDynCheck);
            Label checkFailLabel = new Label("Cast_check_fail_" + numDynCheck);
            Label endCheckLabel = new Label("Cast_check_success_" + numDynCheck);
            Operand castOperand = ((AbstractIdentifier) getLeftOperand()).getClassDefinition().getOperand();
            
            /*
             * We get the heap address of the object to cast on the top of the
             * stack
             */
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.getR(2)));
            
            /*
             * We get the address of the method table of the casting class in
             * the stack (ex 3(GB)) and put it in R3
             */
            compiler.addInstruction(new LEA((DAddr) castOperand, Register.getR(3)));
            
            // do while loop
            compiler.addLabel(whileLabel);

                /*
                 * 1st iter: fetches the @ of the method table of the class of
                 *     the casted object and puts it in R2
                 * Othe iter: fetches the @ of the method table of the super
                 *     class and puts it in R2
                 */
            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.getR(2)), Register.getR(2)));
            compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));

                /*
                 * if R2 is null the casted object is not an instance of the
                 * casting class
                 */
            compiler.addInstruction(new BEQ(checkFailLabel));
            compiler.addInstruction(new CMP(Register.getR(2), Register.getR(3)));

                // while loop condition: R2 != R3 and R2!=null 
            compiler.addInstruction(new BNE(whileLabel));

            compiler.addInstruction(new BRA(endCheckLabel));
            compiler.addLabel(checkFailLabel);
            String casterName = ((AbstractIdentifier) getLeftOperand()).getName().toString();
            compiler.addInstruction(new WSTR("Cast failed: could not be casted to " + casterName));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());

            /*
             * casted object is an instance of the casting class and the
             * relationship degree is the number of times we've been in the
             * loop
             */
            compiler.addLabel(endCheckLabel);

        }

    }
    
    @Override
    public void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        this.getRightOperand().byteGenExp(compiler, mw);
        if (checkDynamically) {
            mw.visitTypeInsn(Opcodes.CHECKCAST, 
                getLeftOperand().getType(). getByteDescriptor());
        }
    }
    
}
