package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.BRA;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Full if/else if/else statement.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class IfThenElse extends AbstractInst {
    
    private final AbstractExpr condition; 
    private final ListInst thenBranch;
    private ListInst elseBranch;

    private static int nbIf = 1;
    private static int nbIfBC = 1;

    public IfThenElse(AbstractExpr condition, ListInst thenBranch, ListInst elseBranch) {
        Validate.notNull(condition);
        Validate.notNull(thenBranch);
        Validate.notNull(elseBranch);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }
    
    @Override
    protected void verifyInst(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass, Type returnType)
            throws ContextualError {

                this.condition.verifyCondition(compiler, localEnv, currentClass);
                this.thenBranch.verifyListInst(compiler, localEnv, currentClass, returnType);
                this.elseBranch.verifyListInst(compiler, localEnv, currentClass, returnType);

    }

    @Override
    protected void codeGenInst(DecacCompiler compiler) {
        
        this.condition.codeGenExp(compiler);
        compiler.addInstruction(new POP(Register.getR(2)));
        compiler.addInstruction(new CMP(0, Register.getR(2)));

        Label endLabel = new Label("endLabel_" + nbIf);
        Label elseLabel = new Label("else_"+ nbIf);
        nbIf++;

        if (this.elseBranch.isEmpty()) {
            compiler.addInstruction(new BEQ(endLabel)); 
            this.thenBranch.codeGenListInst(compiler);
        } else {
            compiler.addInstruction(new BEQ(elseLabel));  
            this.thenBranch.codeGenListInst(compiler);
            compiler.addInstruction(new BRA(endLabel));
            compiler.addLabel(elseLabel);
            this.elseBranch.codeGenListInst(compiler);
        }
        compiler.addLabel(endLabel);

    }

    @Override
    protected void byteGenInst(DecacCompiler compiler, MethodVisitor mv) {       
        this.condition.byteGenExp(compiler, mv);

        // Create labels for jumps in comparison operators
        org.objectweb.asm.Label labelTrue = new org.objectweb.asm.Label();
        org.objectweb.asm.Label labelEnd = new org.objectweb.asm.Label();

        // Comparison with 0 (false), if 1 go to "labelTrue"
        mv.visitJumpInsn(Opcodes.IFNE, labelTrue);

        // Put 0 on stack for false and goto the end label
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitJumpInsn(Opcodes.GOTO, labelEnd);

        // Put 1 on stack for true and finish
        mv.visitLabel(labelTrue);
        mv.visitInsn(Opcodes.ICONST_1);

        // Add end label
        mv.visitLabel(labelEnd);

        // Create labels for jumps in IfThenElse structure
        org.objectweb.asm.Label endLabel = new org.objectweb.asm.Label();
        org.objectweb.asm.Label elseLabel = new org.objectweb.asm.Label();

        if (this.elseBranch.isEmpty()) {

            // Statement comparison with 0, if 0 (false) go to end label
            mv.visitJumpInsn(Opcodes.IFEQ, endLabel); 
            this.thenBranch.byteGenListInst(compiler, mv);

        } else {

            // Statement comparison with 0 (false), if 0 go to else label
            mv.visitJumpInsn(Opcodes.IFEQ, elseLabel); 
            this.thenBranch.byteGenListInst(compiler, mv);

            // Goto end label 
            mv.visitJumpInsn(Opcodes.GOTO, endLabel);

            // Add else label
            mv.visitLabel(elseLabel);
            this.elseBranch.byteGenListInst(compiler, mv);
            
        }

        // Add end label
        mv.visitLabel(endLabel);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("if (");
        condition.decompile(s);
        s.print(") {");
        s.println();
        s.indent();
        thenBranch.decompile(s);
        s.unindent();
        s.print("} else {");
        s.println();
        s.indent();
        elseBranch.decompile(s);
        s.unindent();
        s.print("}");
    }

    @Override
    protected
    void iterChildren(TreeFunction f) {
        condition.iter(f);
        thenBranch.iter(f);
        elseBranch.iter(f);
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        condition.prettyPrint(s, prefix, false);
        thenBranch.prettyPrint(s, prefix, false);
        elseBranch.prettyPrint(s, prefix, true);
    }
}
