package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.WINT;
import fr.ensimag.ima.pseudocode.instructions.WFLOAT;
import fr.ensimag.ima.pseudocode.instructions.WFLOATX;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Print statement (print, println, ...).
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class AbstractPrint extends AbstractInst {

    private boolean printHex;
    private ListExpr arguments = new ListExpr();
    
    abstract String getSuffix();

    public AbstractPrint(boolean printHex, ListExpr arguments) {
        Validate.notNull(arguments);
        this.arguments = arguments;
        this.printHex = printHex;
    }

    public ListExpr getArguments() {
        return arguments;
    }

    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {

        arguments.verifyListExprPrint(compiler, localEnv, currentClass);

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {

        for (AbstractExpr a : getArguments().getList()) {

            a.codeGenPrint(compiler);

            if (a.getType().isInt()) {
                compiler.addInstruction(new WINT());
            }
            else if (a.getType().isFloat()) {

                if (getPrintHex()) {
                    compiler.addInstruction(new WFLOATX());
                }
                else {
                    compiler.addInstruction(new WFLOAT());
                }

            }

        }

    }

    @Override
    protected void byteGenInst(DecacCompiler compiler, MethodVisitor mw) {

        for (AbstractExpr a : getArguments().getList()) {

            // pushes the 'out' field (of type PrintStream) of the System class
            mw.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
                "Ljava/io/PrintStream;"); 

            a.byteGenPrint(compiler, mw);
            String eltAAfficher = "NonInitialisé";

            /* 
             * sets on what the 'print' (defined in the PrintStream class)
             * method is invoked: INT
             */
            if(a.getType().isInt()) {
                eltAAfficher = "(I)V";
            } 

            // sets on what the 'print'method is invoked: FLOAT
            else if(a.getType().isFloat()) {
                eltAAfficher = "(F)V";        
            }

            // sets on what the 'print'method is invoked: STRING
            else if(a.getType().isString()) {
                eltAAfficher = "(Ljava/lang/String;)V";
            }
            
            if(!eltAAfficher.equals("NonInitialisé")) {
                mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
                    "print", eltAAfficher, false);
            }
            
        }

    }

    private boolean getPrintHex() {
        return printHex;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("print" + getSuffix() + "(");
        arguments.decompile(s);
        s.print(");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        arguments.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        arguments.prettyPrint(s, prefix, true);
    }

}
