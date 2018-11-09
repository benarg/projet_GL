package fr.ensimag.deca.tree;

import java.io.PrintStream;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.DAddr;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Absence of initialization (e.g. "int x;" as opposed to "int x =
 * 42;").
 *
 * @author gl56
 * @date 01/01/2017
 */
public class NoInitialization extends AbstractInitialization {

    @Override
    protected void verifyInitialization(DecacCompiler compiler, Type t,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
        //there is nothing to implement
        //throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenInitialization(DecacCompiler compiler, DAddr operand) {
        // Nothing to do here
    }

    @Override
    protected void byteGenInitialization(DecacCompiler compiler, int id, MethodVisitor mv) {
        // Nothing to do here
    }

    @Override
    protected void byteGenInitField(AbstractIdentifier fieldName, MethodVisitor mw) {
        // we load 'this'
        mw.visitVarInsn(Opcodes.ALOAD,0);
        if (fieldName.getFieldDefinition().getType().isClass()) {
            mw.visitInsn(Opcodes.ACONST_NULL);
        } else if(fieldName.getFieldDefinition().getType().isFloat()){
            mw.visitInsn(Opcodes.FCONST_0);
        } else {
            mw.visitInsn(Opcodes.ICONST_0);
        }
        String className = fieldName.getFieldDefinition().getContainingClass().getType().getByteDescriptor();
        String name = fieldName.getName().toString();
        String signature = fieldName.getFieldDefinition().getType().getByteDescriptor();
            if (fieldName.getFieldDefinition().getType().isClass())
                signature = "L" + signature + ";";
        mw.visitFieldInsn(Opcodes.PUTFIELD, className, name, signature);
    }


    /**
     * Node contains no real information, nothing to check.
     */
    @Override
    protected void checkLocation() {
        // nothing
    }

    @Override
    public void decompile(IndentPrintStream s) {
        // nothing
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
