package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.context.IntType;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.RINT;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.PrintStream;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class ReadInt extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        
            Type thisType = new IntType(compiler.getSymbolTable().create("int"));
            setType(thisType);
            return thisType;

    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        compiler.addInstruction(new RINT());
        compiler.setIncrementeVariableTemporaires(1);
        if (compiler.getVariableTemporaires() > compiler.getVariableTemporaireMax())
            compiler.setVariableTemporaireMax(compiler.getVariableTemporaires());
        compiler.addInstruction(new PUSH(Register.getR(1)));
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mv) {
        // Create new scanner
        mv.visitTypeInsn(Opcodes.NEW, "java/util/Scanner");
        mv.visitInsn(Opcodes.DUP);

        //// Put reference to System.in on stack
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", "Ljava/io/InputStream;");

        // Call constructor with argument and read next Int
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", 
            org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.VOID_TYPE, 
                org.objectweb.asm.Type.getType("Ljava/io/InputStream;")), false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextInt", 
            org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.INT_TYPE), false);
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new RINT());
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readInt()");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }

}
