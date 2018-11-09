package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Line;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import fr.ensimag.ima.pseudocode.instructions.WNL;
import fr.ensimag.ima.pseudocode.instructions.TSTO;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import fr.ensimag.ima.pseudocode.instructions.ADDSP;
import fr.ensimag.ima.pseudocode.instructions.HALT;
import fr.ensimag.ima.pseudocode.instructions.BOV;
import bytecode.BytegenError;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Deca complete program (class definition plus main block)
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Program extends AbstractProgram {
    private static final Logger LOG = Logger.getLogger(Program.class);
    
    public Program(ListDeclClass classes, AbstractMain main) {
        Validate.notNull(classes);
        Validate.notNull(main);
        this.classes = classes;
        this.main = main;
    }
    public ListDeclClass getClasses() {
        return classes;
    }
    public AbstractMain getMain() {
        return main;
    }
    private ListDeclClass classes;
    private AbstractMain main;

    @Override
    public void verifyProgram(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify program: start");

        classes = getClasses();
        main = getMain();

        // PASS 1
        classes.verifyListClass(compiler);

        // PASS 2
        classes.verifyListClassMembers(compiler);
        
        // PASS 3
        classes.verifyListClassBody(compiler);
        main.verifyMain(compiler);
	
        // LOG.debug("verify program: end");
    }

    public void createLabel(DecacCompiler compiler, String nomError, String messageError) {
        if(!compiler.getCompilerOptions().getNoCheck()) {
            Label erreurLabel = new Label(nomError);
            compiler.addLabel(erreurLabel);
            compiler.addInstruction(new WSTR(messageError));
            compiler.addInstruction(new WNL());
            compiler.addInstruction(new ERROR());
        }
    }
    
    
    @Override
    public void codeGenProgram(DecacCompiler compiler) {

        getClasses().codeGenListDeclClass(compiler);

        compiler.addComment("Main program");
        main.codeGenMain(compiler);
        compiler.addInstruction(new HALT());
        compiler.addComment("End main program");

        compiler.addComment("Method codes of classes");
        getClasses().codeGenListDeclClassMethods(compiler);

        compiler.addFirst(new Line(new ADDSP(compiler.getVariableGlobales())));
        if(!compiler.getCompilerOptions().getNoCheck())
            compiler.addFirst(new Line(new BOV(new Label("stack_overflow_error"))));
        compiler.addFirst(new Line(new TSTO(compiler.getVariableTemporaireMax() + compiler.getVariableGlobales())));
        compiler.addComment("Messages d'erreurs");
        createLabel(compiler, "overflow_error", "Error: Overflow during arithmetic operation");
        createLabel(compiler, "stack_overflow_error", "Error: Stack Overflow");
        createLabel(compiler, "dereferencement.null", "Error : dereferencement de null");
        createLabel(compiler, "divide_by_zero", "Error : you divided by zero");
        
    }

    @Override
    public void byteGenProgram(DecacCompiler compiler) throws BytegenError {
        // creates a MethodWriter for the (implicit) constructor
        MethodVisitor mw = compiler.getCW().visitMethod(Opcodes.ACC_PUBLIC, 
            "<init>", "()V", null, null);

        // pushes the 'this' variable
        mw.visitVarInsn(Opcodes.ALOAD, 0);

        // invokes the super class constructor
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", 
            "<init>", "()V", false);
        mw.visitInsn(Opcodes.RETURN);

        // Automatic setting for the stack size and local variables
        mw.visitMaxs(0, 0);
        mw.visitEnd();

        if (!classes.isEmpty()){
            classes.byteGenListDeclClass(compiler);
            //throw new BytegenError("Bytegen with Objects is not yet implemented", getLocation());
        }

        main.byteGenMain(compiler);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        getClasses().decompile(s);
        getMain().decompile(s);
    }
    
    @Override
    protected void iterChildren(TreeFunction f) {
        classes.iter(f);
        main.iter(f);
    }
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        classes.prettyPrint(s, prefix, false);
        main.prettyPrint(s, prefix, true);
    }
}
