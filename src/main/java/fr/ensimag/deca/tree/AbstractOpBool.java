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
public abstract class AbstractOpBool extends AbstractBinaryExpr {

    public AbstractOpBool(AbstractExpr leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        
        this.getLeftOperand().verifyCondition(compiler,localEnv,currentClass);
        this.getRightOperand().verifyCondition(compiler,localEnv,currentClass);

        Symbol boolSymbol = compiler.getSymbolTable().create("boolean");
        Type tBoolType = new BooleanType(boolSymbol);
        this.setType(tBoolType);

        return tBoolType;
    }

}
