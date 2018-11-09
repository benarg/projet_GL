package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import java.io.PrintStream;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl56
 */
public class This extends AbstractLValue {
    

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError { 
                if (currentClass == null) {
                    throw new ContextualError("Cannot use 'this' outside of a class ", this.getLocation());
                }
                this.setType(currentClass.getType());
                return currentClass.getType();
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        compiler.addInstruction(new LOAD(new RegisterOffset(-2, Register.LB), Register.getR(2)));
        compiler.setIncrementeVariableTemporaires(1);
        if (compiler.getVariableTemporaires() > compiler.getVariableTemporaireMax())
            compiler.setVariableTemporaireMax(compiler.getVariableTemporaires());
        compiler.addInstruction(new PUSH(Register.getR(2)));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        mw.visitVarInsn(Opcodes.ALOAD,0);
    }

    @Override
    String prettyPrintNode() {
        return "This";
    }

    @Override
    protected void iterChildren(TreeFunction f) {
   
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {

    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("this");
    }
        
}
