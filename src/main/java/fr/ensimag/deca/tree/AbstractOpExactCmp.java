package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.SEQ;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.Register;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractOpExactCmp extends AbstractOpCmp {

    public AbstractOpExactCmp(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type tLeftOp = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
    	Type tRightOp = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (tLeftOp.isClassOrNull() && tRightOp.isClassOrNull()) {
        	// nothing
        } else {

            if (!(tLeftOp.isFloat() || tLeftOp.isInt() || tLeftOp.isBoolean())) {
                throw new ContextualError("Expected int, float or boolean got " + tLeftOp + 
                    " (rule 3.33)", this.getLeftOperand().getLocation());
            }

            if (!(tRightOp.isFloat() || tRightOp.isInt() || tRightOp.isBoolean())) {
                throw new ContextualError("Expected int, float or boolean got " + tRightOp + 
                    " (rule 3.33)", this.getRightOperand().getLocation());
            }

            if (!(tRightOp.sameType(tLeftOp))) {

                if (tRightOp.isBoolean() || tLeftOp.isBoolean()) {
                    throw new ContextualError("Expected " + tLeftOp + " got " + tRightOp +
                        " (rule 3.33)", this.getRightOperand().getLocation());
                } else {
                    if (tRightOp.isFloat()) {
                        this.setLeftOperand(new ConvFloat(this.getLeftOperand()));
                        this.getLeftOperand().setType(tRightOp);
                        tLeftOp = tRightOp;
                    } else {
                        this.setRightOperand(new ConvFloat(this.getRightOperand()));
                        this.getRightOperand().setType(tLeftOp);
                    }
                }

            }

        }

  		Symbol boolSymbol = compiler.getSymbolTable().create("boolean");
  		Type tBoolType = new BooleanType(boolSymbol);
        this.setType(tBoolType);

       	return tBoolType;

    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {

        if (getLeftOperand().getType().isClass()) {

            getRightOperand().codeGenExp(compiler);
            getLeftOperand().codeGenExp(compiler);
            compiler.setIncrementeVariableTemporaires(-1);

            ClassDefinition leftOpClassDef = ((ClassType) getLeftOperand().getType()).getDefinition();
            Symbol equalsSymbol = compiler.getSymbolTable().create("equals");
            // Look for the equals method in the local envt of the class 
            MethodDefinition methodDef = (MethodDefinition) leftOpClassDef.getMembers().get(equalsSymbol);

            compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.getR(2)));
            compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));

            if (getRightOperand().getType().isNull()) {
                compiler.addInstruction(new SEQ(Register.getR(0)));
            } else {
                compiler.addInstruction(new BEQ(new Label("dereferencement.null")));        
                compiler.addInstruction(new BSR(methodDef.getLabel()));
            }

            compiler.addInstruction(new SUBSP(2));

        } else {
            super.codeGenExp(compiler);
        }
    }

}
