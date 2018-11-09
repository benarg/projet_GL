package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;

/**
 * Arithmetic binary operations (+, -, /, ...)
 * 
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractOpArith extends AbstractBinaryExpr {

    public AbstractOpArith(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

    	Type tLeftOp = this.getLeftOperand().verifyExpr(compiler, localEnv, currentClass);
    	Type tRightOp = this.getRightOperand().verifyExpr(compiler, localEnv, currentClass);

        // We check first if the operands are of type INT or FLOAT
        if (!(tLeftOp.isFloat() || tLeftOp.isInt())) {
            throw new ContextualError("Expected int or float got " + tLeftOp + 
                " (rule 3.33)", this.getLeftOperand().getLocation());
        }

        if (!(tRightOp.isFloat() || tRightOp.isInt())) {
            throw new ContextualError("Expected int or float got " + tRightOp + 
                " (rule 3.33)", this.getRightOperand().getLocation());
        }

        // We then check whether they're of the same type
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
  
        this.setType(tLeftOp);
       	return tLeftOp;
    }
}
