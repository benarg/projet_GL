package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.VoidType;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class Main extends AbstractMain {
    private static final Logger LOG = Logger.getLogger(Main.class);
    
    private ListDeclVar declVariables;
    private ListInst insts;
    public Main(ListDeclVar declVariables,
            ListInst insts) {
        Validate.notNull(declVariables);
        Validate.notNull(insts);
        this.declVariables = declVariables;
        this.insts = insts;
    }

    @Override
    protected void verifyMain(DecacCompiler compiler) throws ContextualError {
        LOG.debug("verify Main: start");
	
        // A FAIRE: Appeler méthodes "verify*" de ListDeclVarSet et ListInst.
        // Vous avez le droit de changer le profil fourni pour ces méthodes
        // (mais ce n'est à priori pas nécessaire).

	SymbolTable symbolTable = compiler.getSymbolTable();
	Symbol voidSymbol = symbolTable.create("void");	
	VoidType voidReturn = new VoidType(voidSymbol);
	
	EnvironmentExp localEnv = new EnvironmentExp(null);

	declVariables.verifyListDeclVariable(compiler, localEnv, null);
	insts.verifyListInst(compiler, localEnv, null, voidReturn);

        LOG.debug("verify Main: end");

	// throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    protected void codeGenMain(DecacCompiler compiler) {
        compiler.addComment("Beginning declaration of main variables:");
        declVariables.codeGenListDeclVar(compiler, null);
        compiler.addComment("Beginning of main instructions:");
        insts.codeGenListInst(compiler);
    }

    @Override
    protected void byteGenMain(DecacCompiler compiler) {
        // Creates a MethodWriter for the 'main' method
        MethodVisitor mw = compiler.getCW().visitMethod(Opcodes.ACC_PUBLIC + 
            Opcodes.ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);

        declVariables.byteGenListDeclVar(compiler,null, mw, 0);
        insts.byteGenListInst(compiler, mw);
        mw.visitInsn(Opcodes.RETURN);
        
        // Automatic setting for the stack size and local variables
        mw.visitMaxs(0, 0);
        mw.visitEnd();
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        s.println("{");
        s.indent();
        declVariables.decompile(s);
        insts.decompile(s);
        s.unindent();
        s.println("}");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        declVariables.iter(f);
        insts.iter(f);
    }
 
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        declVariables.prettyPrint(s, prefix, false);
        insts.prettyPrint(s, prefix, true);
    }
}
