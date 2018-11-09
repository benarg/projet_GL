package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.BooleanType;
import fr.ensimag.deca.tools.SymbolTable.Symbol;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractOpIneq extends AbstractOpCmp {

    public AbstractOpIneq(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Type tLeftOp = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
    	Type tRightOp = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        if (!(tLeftOp.isFloat() || tLeftOp.isInt())) {
            throw new ContextualError("Expected int or float got " + tLeftOp + 
                " (rule 3.33)", this.getLeftOperand().getLocation());
        }
        if (!(tRightOp.isFloat() || tRightOp.isInt())) {
            throw new ContextualError("Expected int or float got " + tRightOp + 
                " (rule 3.33)", this.getRightOperand().getLocation());
        }

        if (!(tRightOp.sameType(tLeftOp))) {

            if (tRightOp.isFloat()) {
                this.setLeftOperand(new ConvFloat(this.getLeftOperand()));
                this.getLeftOperand().setType(tRightOp);
                tLeftOp = tRightOp;
            } else {
                this.setRightOperand(new ConvFloat(this.getRightOperand()));
                this.getRightOperand().setType(tLeftOp);
            }

        }

  		Symbol boolSymbol = compiler.getSymbolTable().create("boolean");
  		Type tBoolType = new BooleanType(boolSymbol);
        this.setType(tBoolType);
        
       	return tBoolType;
    }

}
