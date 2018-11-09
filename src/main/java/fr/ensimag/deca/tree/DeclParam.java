package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ParamDefinition;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;


/**
 *
 * @author gl56
 */
public class DeclParam extends AbstractDeclParam{
    
    protected AbstractIdentifier nameParam;
    protected AbstractIdentifier type;
    
    public DeclParam(AbstractIdentifier type, AbstractIdentifier nameParam) {
            this.nameParam = nameParam;
            this.type = type;
    }
    
    @Override
    protected Type verifyDeclParam(DecacCompiler compiler, int stackIndex)
            throws ContextualError {
        
        Type tType = type.verifyType(compiler);
        nameParam.resetName(compiler.getSymbolTable());

        if (tType.isNull() || tType.isVoid()) {
            throw new ContextualError("Cannot have void type parameter (rule 2.9)",
                                      type.getLocation());
        }
        nameParam.setType(tType);
        nameParam.setDefinition(new ParamDefinition(tType, nameParam.getLocation()));
        nameParam.getExpDefinition().setOperand(new RegisterOffset(-(stackIndex + 2),
                                                Register.LB));
        return tType;

    }

    @Override
    protected ParamDefinition verifyDeclParam_pass3(DecacCompiler compiler) 
            throws ContextualError {

        Type returnType = type.verifyType(compiler);
        ParamDefinition returnDef = (ParamDefinition) nameParam.getDefinition();
        return returnDef;

    }

    @Override
    public String getByteDesc(int numDeclared) {
        nameParam.getExpDefinition().setId(numDeclared);
        if (type.getType().isFloat()) {
            return "F";
        } else if (type.getType().isClass()) {
            if (type.getName().toString().equals("Object"))
                return "Ljava/lang/Object;";
            else
                return "L" + type.getName() + ";";
        } else {
            return "I";
        }
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        nameParam.prettyPrint(s, prefix, true);
    }

    @Override
    protected AbstractIdentifier getName() {
        return this.nameParam;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        nameParam.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        nameParam.iter(f);
        type.iter(f);
    }
}
