package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.util.Iterator;

/**
 *
 * @author gl56
 */
public class ListDeclParam extends TreeList<AbstractDeclParam>{

    @Override
    public void decompile(IndentPrintStream s) {
        AbstractDeclParam a;
        Iterator<AbstractDeclParam> ite = this.iterator();
        if (ite.hasNext()) {
            a = ite.next();
            a.decompile(s);
        }
        while (ite.hasNext()) {
            s.print(" , ");
            a = ite.next();
            a.decompile(s);
        }
    }

    public Signature verifyListParams(DecacCompiler compiler) throws ContextualError {
    	Signature sig = new Signature();
        int stackIndex = 1;
    	for (AbstractDeclParam param : getList()){
    		sig.add(param.verifyDeclParam(compiler, stackIndex));
            stackIndex++;
    	}
    	return sig;
    }

    public void verifyListParams_pass3(DecacCompiler compiler, EnvironmentExp envExp) throws ContextualError {
        
        for (AbstractDeclParam param : getList()) {

            // Difference from grammar defined in pass 3:
            //   We return a paramDef instead of an EnvExp containing a paramDef
            
            AbstractIdentifier nameParam = param.getName();
            ParamDefinition paramDef = param.verifyDeclParam_pass3(compiler);

            try {
                envExp.declare(nameParam.getName(), paramDef);
            } catch (EnvironmentExp.DoubleDefException e) {
                throw new ContextualError(nameParam.getName() + " was already declared here: " + 
                envExp.get(nameParam.getName()).getLocation() + " (rule 3.17)", param.getLocation());
            }
        }
    }

    public String getByteDesc() {
        String s = "(";
        int numDeclared = 1;
        for (AbstractDeclParam param : getList()) {
            s += param.getByteDesc(numDeclared);
            numDeclared++;
        }
        return s + ")";
    }
    
}
