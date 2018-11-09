package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;

import java.util.Iterator;
import java.util.List;

/**
 * List of expressions (eg list of parameters).
 *
 * @author gl56
 * @date 01/01/2017
 */
public class ListExpr extends TreeList<AbstractExpr> {


    @Override
    public void decompile(IndentPrintStream s) {
        Iterator<AbstractExpr> ite = this.iterator();
        AbstractExpr e;
        if (ite.hasNext()) {
        	e = ite.next();
        	e.decompile(s);
			while (ite.hasNext()) {
				s.print(", ");
				e = ite.next();
				e.decompile(s);
			}
		}
    }

	public void verifyListExpr(DecacCompiler compiler, EnvironmentExp localEnv, 
		ClassDefinition currentClass, List<Type> listTypes) throws ContextualError {

		int index = 0;

		if (this.size() != listTypes.size()) {
			throw new ContextualError("Expected " + listTypes.size() 
                + " arguments got " + this.size() + " arguments (rule 3.73-3.74)", this.getLocation());
		}

		for (AbstractExpr a : getList()) {
			
			Type type = a.verifyExpr(compiler, localEnv, currentClass);

			if (!(type.sameType(listTypes.get(index)))) {
				throw new ContextualError("Expected " + listTypes.get(index) 
                    + " got " + type + " (rule 3.73-3.74)", this.getLocation());
			}

			index++;

		}
	}

    /**
     * Implements non-terminal "list_expr" in pass 3
     * @param compiler contains "env_types"
     * @param localEnv corresponds to "env_exp" attribute
     * @param currentClass corresponds to "class" attribute
     */
    public void verifyListExprPrint(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) throws ContextualError {

		Iterator<AbstractExpr> ite = this.iterator();
		
		while (ite.hasNext()) {

		    AbstractExpr e = ite.next();
		    
		    Type t = e.verifyExpr(compiler, localEnv, currentClass);
		    
		    if (!(t.isFloat() || t.isString() || t.isInt())) {
			
				throw new ContextualError
				    ("Expected int, float or String got " + t + " (rule 3.31)", e.getLocation());
			
		    }
		    
		}
	
    }
    
}
