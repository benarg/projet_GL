package fr.ensimag.deca.tree;

import java.io.PrintStream;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.NEW;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Left-hand side value of an assignment.
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class New extends AbstractExpr {

    AbstractIdentifier className;

    public New(AbstractIdentifier className) {
        this.className = className;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {

            Type classType = this.className.verifyType(compiler);

            this.setType(classType);

            return classType;

    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {

        ClassDefinition classDef = className.getClassDefinition();
        int numFields = classDef.getNumberOfFields();

        compiler.addInstruction(new NEW(numFields + 1, Register.getR(2)));
        compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
        compiler.addInstruction(new LEA(classDef.getOperand(), Register.getR(0)));
        compiler.addInstruction(new STORE(Register.getR(0), new RegisterOffset(0, Register.getR(2))));
        compiler.addInstruction(new PUSH(Register.getR(2)));
        compiler.addInstruction(new BSR(new Label("init." + className.getName().getName())));
        
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        String classNameString = className.getName().toString();
        if (classNameString.equals("Object"))
            classNameString = "java/lang/Object";
        mw.visitTypeInsn(Opcodes.NEW, classNameString);
        mw.visitInsn(Opcodes.DUP);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, classNameString, "<init>", "()V",
                false);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("new " + className.getName() + "()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {

        className.prettyPrint(s, prefix, true);
        
    }

}
