package fr.ensimag.deca.tree;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Expression, i.e. anything that has a value.
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractExpr extends AbstractInst {
    /**
     * @return true if the expression does not correspond to any concrete token
     * in the source code (and should be decompiled to the empty string).
     */
    boolean isImplicit() {
        return false;
    }

    /**
     * Get the type decoration associated to this expression (i.e. the type computed by contextual verification).
     */
    public Type getType() {
        return type;
    }

    protected void setType(Type type) {
        Validate.notNull(type);
        this.type = type;
    }
    private Type type;

    @Override
    protected void checkDecoration() {

        if (getType() == null) {
            throw new DecacInternalError("Expression " + decompile() + " has no Type decoration");
        }
    }

    /**
     * Verify the expression for contextual error.
     * 
     * implements non-terminals "expr" and "lvalue" 
     *    of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  (contains the "env_types" attribute)
     * @param localEnv
     *            Environment in which the expression should be checked
     *            (corresponds to the "env_exp" attribute)
     * @param currentClass
     *            Definition of the class containing the expression
     *            (corresponds to the "class" attribute)
     *             is null in the main bloc.
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public abstract Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError;

    /**
     * Verify the expression in right hand-side of (implicit) assignments 
     * 
     * implements non-terminal "rvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute
     * @param expectedType corresponds to the "type1" attribute            
     * @return this with an additional ConvFloat if needed...
     */
    public AbstractExpr verifyRValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass, 
            Type expectedType)
            throws ContextualError {

                Type returnType = this.verifyExpr(compiler, localEnv, currentClass);


                if (expectedType.isFloat() && returnType.isInt()) {
                    AbstractExpr returnExpr = new ConvFloat(this);
                    returnExpr.setType(expectedType);
                    return returnExpr;
                }

                if (! returnType.sameType(expectedType)) {
                    throw new ContextualError("expected " + expectedType + " got " + 
                        returnType + " (rule 3.28)", this.getLocation());
                }
                return this;

        }


    /**
     * Verify the expression in left hand-side of (implicit) assignments 
     * 
     * implements non-terminal "lvalue" of [SyntaxeContextuelle] in pass 3
     *
     * @param compiler  contains the "env_types" attribute
     * @param localEnv corresponds to the "env_exp" attribute
     * @param currentClass corresponds to the "class" attribute          
     * @return the Type of the expression
     *            (corresponds to the "type" attribute)
     */
    public Type verifyLValue(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
            return this.verifyExpr(compiler, localEnv, currentClass);
        }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {
                verifyExpr(compiler, localEnv, currentClass);
    }

    /**
     * Verify the expression as a condition, i.e. check that the type is
     * boolean.
     *
     * @param localEnv
     *            Environment in which the condition should be checked.
     * @param currentClass
     *            Definition of the class containing the expression, or null in
     *            the main program.
     */
    void verifyCondition(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
       
                Type thisType = this.verifyExpr(compiler, localEnv, currentClass);

                if (!(thisType.isBoolean())) {
                    throw new ContextualError("Expected boolean got " + thisType +
                    " (rule 3.29)", this.getLocation());
                }

    }

    /**
     * Generates IMA instructions to print the expression
     *
     * @param compiler
     */
    protected void codeGenPrint(DecacCompiler compiler) {
        this.codeGenExp(compiler);
        compiler.setIncrementeVariableTemporaires(-1);
        compiler.addInstruction(new POP(Register.R1));
    }

    /**
     * Generates Java bytecode to print the expression
     * 
     * @param compiler [the compiler]
     * @param mw       [the Method Visitor]
     */
    protected void byteGenPrint(DecacCompiler compiler, MethodVisitor mw) {
        this.byteGenExp(compiler, mw);
    }

    /**
     * Generates IMA instructions for an instruction by calling codeGenExp
     * clears the stack afterward
     * 
     * @param compiler [the compiler]
     */
    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        this.codeGenExp(compiler);
        if (!(this.getType().isVoid()))
            compiler.addInstruction(new SUBSP(1));
    }


    /**
     * Generates Java bytecode for an instruction by calling byteGenExp
     * clears the stack afterward
     * 
     * @param compiler [the compiler]
     * @param mw       [the Method Visitor]
     */
    @Override
    protected void byteGenInst(DecacCompiler compiler, MethodVisitor mw) {
        this.byteGenExp(compiler, mw);
        if (!(this.getType().isVoid()))
            mw.visitInsn(Opcodes.POP);     
    }

    protected void codeGenExp(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }
    
    protected void codeGenAssign(DecacCompiler compiler) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void decompileInst(IndentPrintStream s) {
        decompile(s);
        s.print(";");
    }

    @Override
    protected void prettyPrintType(PrintStream s, String prefix) {
        Type t = getType();
        if (t != null) {
            s.print(prefix);
            s.print("type: ");
            s.print(t);
            s.println();
        }
    }
}
