package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.RTS;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl56
 */
public class MethodBody extends AbstractMethodBody {

    private ListDeclVar decls; // global variables declared in the method
    private ListInst insts;    // list of instructions of the method
    
    @Override
    public ListInst getInsts() {
        return this.insts;
    }

    public ListDeclVar getDeclVars() {
        return this.decls;
    }

    public MethodBody(ListDeclVar decls, ListInst insts) {
        this.decls = decls;
        this.insts = insts;
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        decls.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envExp, 
        EnvironmentExp envExpMethod, ClassDefinition currentClass, Type tType) 
        throws ContextualError {

            decls.verifyListDeclVariable(compiler, envExpMethod, currentClass);
	        insts.verifyListInst(compiler, envExpMethod, currentClass, tType);

    }

    protected void codeGenInst(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared,
        AbstractIdentifier methodName) {

        MethodDefinition methodDef = methodName.getMethodDefinition();
        compiler.addLabel(methodDef.getLabel()); 
        compiler.addInstruction(new TSTO(2));
        if(methodDef.getNbLocals() != 0)
            compiler.addInstruction(new ADDSP(methodDef.getNbLocals())); // For local variables
        compiler.addComment("Sauvegarde des registres");  
        compiler.addInstruction(new PUSH(Register.getR(2)));
        compiler.addInstruction(new PUSH(Register.getR(3)));
        compiler.addInstruction(new BOV(new Label("stack_overflow_error")));
        compiler.addComment("Corps de la méthode");
        this.getDeclVars().codeGenListDeclVar(compiler, currentClass);
        this.getInsts().codeGenListInst(compiler);
        compiler.addComment("Restauration des registres");
        compiler.addInstruction(new POP(Register.getR(3)));
        compiler.addInstruction(new POP(Register.getR(2)));
        if(methodDef.getNbLocals() != 0)
            compiler.addInstruction(new SUBSP(methodDef.getNbLocals()));
        if (!(methodDef.getType().isVoid())) {
            compiler.addInstruction(new WSTR("Error : missing return statement in the method " 
                + methodName.getName()));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
            compiler.addLabel(new Label("fin." + currentClass.getType() + "." + methodName.getName()));
        }
        compiler.addInstruction(new RTS());

    }

    @Override
    protected void byteGenInst(DecacCompiler compiler, ClassDefinition currentClass, 
            MethodVisitor mw, int numParams) {
        this.getDeclVars().byteGenListDeclVar(compiler, currentClass, mw, numParams);
        this.getInsts().byteGenListInst(compiler, mw);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        decls.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        decls.iter(f);
        insts.iter(f);
    }

}
