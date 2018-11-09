package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.FloatType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.RFLOAT;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import java.io.PrintStream;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 *
 * @author gl56
 * @date 01/01/2017
 */
public class ReadFloat extends AbstractReadExpr {

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

        Symbol floatSymbol = compiler.getSymbolTable().create("float");
        FloatType thisType = new FloatType(floatSymbol);
        setType(thisType);
            
        return thisType; 
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new RFLOAT());
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {
        compiler.addInstruction(new RFLOAT());
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
        mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "in", 
            "Ljava/io/InputStream;");

        // Call constructor with argument and read next Float
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/util/Scanner", "<init>", 
            org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.VOID_TYPE, 
                org.objectweb.asm.Type.getType("Ljava/io/InputStream;")), false);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/util/Scanner", "nextFloat", 
            org.objectweb.asm.Type.getMethodDescriptor(org.objectweb.asm.Type.FLOAT_TYPE), false);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("readFloat()");
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
