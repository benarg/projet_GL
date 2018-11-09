package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.VariableDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class DeclVar extends AbstractDeclVar {

    
    final private AbstractIdentifier type;
    final private AbstractIdentifier varName;
    final private AbstractInitialization initialization;

    public DeclVar(AbstractIdentifier type, AbstractIdentifier varName, AbstractInitialization initialization) {
        Validate.notNull(type);
        Validate.notNull(varName);
        Validate.notNull(initialization);
        this.type = type;
        this.varName = varName;
        this.initialization = initialization;
    }

    @Override
    protected void verifyDeclVar(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

        type.resetName(compiler.getSymbolTable());
        varName.resetName(compiler.getSymbolTable());
        
        Type typeOfAttributeType = type.verifyType(compiler);
        if (typeOfAttributeType.isVoid()) {
            throw new ContextualError("Cannot have void type variable (rule 3.17)", type.getLocation());
        }

        initialization.verifyInitialization(compiler, typeOfAttributeType, localEnv, currentClass);

        ExpDefinition expDef = new VariableDefinition(typeOfAttributeType, varName.getLocation());

        varName.setDefinition(expDef);
        varName.setType(typeOfAttributeType);

        try {
            localEnv.declare(varName.getName(), expDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(varName.getName() + " was already declared here: " + 
                localEnv.get(varName.getName()).getLocation() + " (rule 3.17)", this.getLocation());
        } 
    }

    @Override
    public void codeGenDeclVar(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared) {
        ExpDefinition expDef = varName.getExpDefinition();
        if (currentClass == null) {
            compiler.addVariableGlobales(1);
            expDef.setOperand(new RegisterOffset(numDeclared + 1, Register.GB));
        } else {
            expDef.setOperand(new RegisterOffset(numDeclared + 1, Register.LB));
        }
        this.initialization.codeGenInitialization(compiler, expDef.getOperand());
            compiler.addComment(type.getName() + " " + varName.getName() + 
                " is defined and stored in " + expDef.getOperand());
    }

    @Override
    public void byteGenDeclVar(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared, MethodVisitor mv) {
        //if (currentClass == null) {
            ExpDefinition expDef = varName.getExpDefinition();
            expDef.setId(numDeclared);
            this.initialization.byteGenInitialization(compiler, numDeclared, mv);
        //}
    }

    
    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        varName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        varName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        varName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }
}
