package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Operand;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.DAddr;
import fr.ensimag.ima.pseudocode.ImmediateInteger;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BNE;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class InstanceOf extends AbstractOpCmp {

    private static int numInstCheck = 0;

    public InstanceOf(AbstractExpr leftOperand, AbstractIdentifier rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        Type tLeftOp = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
        Type tRightOp = ((AbstractIdentifier) this.getRightOperand()).verifyType(compiler);
        if (!(tLeftOp.isClassOrNull())) {
            throw new ContextualError("Expected Object or Null got " + tLeftOp + 
                    " (rule 3.40)", this.getLeftOperand().getLocation());
        }
        if (!(tRightOp.isClass())) {
            throw new ContextualError("Expected Object got " + tRightOp + 
                    " (rule 3.40)", this.getRightOperand().getLocation());
        }

        Symbol boolSymbol = compiler.getSymbolTable().create("boolean");
        Type tBoolType = new BooleanType(boolSymbol);
        this.setType(tBoolType);
        return tBoolType;
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        numInstCheck++;
        getLeftOperand().codeGenExp(compiler);
        Label whileLabel = new Label("Instance_check_" + numInstCheck);
        Label checkFailLabel = new Label("Instance_check_fail_" + numInstCheck);
        Label checkSuccessLabel = new Label("Instance_check_success_" + numInstCheck);
        Label endCheckLabel = new Label("Instance_check_end_" + numInstCheck);
        Operand castOperand = ((AbstractIdentifier) getRightOperand()).getClassDefinition().getOperand();
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));
        compiler.addInstruction(new BEQ(checkFailLabel));
        compiler.addInstruction(new LEA((DAddr) castOperand, Register.getR(3)));
        // do while loop
        compiler.addLabel(whileLabel);
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.getR(2)), Register.getR(2)));
        compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));
        compiler.addInstruction(new BEQ(checkFailLabel));
        compiler.addInstruction(new CMP(Register.getR(2), Register.getR(3)));
        compiler.addInstruction(new BNE(whileLabel));
        compiler.addInstruction(new BRA(checkSuccessLabel));
        compiler.addLabel(checkFailLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(0), Register.getR(2)));
        compiler.addInstruction(new BRA(endCheckLabel));
        compiler.addLabel(checkSuccessLabel);
        compiler.addInstruction(new LOAD(new ImmediateInteger(1), Register.getR(2)));
        compiler.addLabel(endCheckLabel);
        compiler.addInstruction(new PUSH(Register.getR(2)));

    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        getLeftOperand().byteGenExp(compiler, mw);
        mw.visitTypeInsn(Opcodes.INSTANCEOF, 
            getRightOperand().getType(). getByteDescriptor());
    }

    @Override
    protected String getOperatorName() {
        return " instanceof ";
    }    
    
}
