package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.ima.pseudocode.InlinePortion;
import fr.ensimag.deca.tools.IndentPrintStream;
import java.io.PrintStream;

import org.objectweb.asm.MethodVisitor;

/**
 *
 * @author gl56
 */
public class MethodBodyASM extends AbstractMethodBody {
    
    private String asmString;

    @Override
    public ListInst getInsts() {
        return null;
    }

    public MethodBodyASM(String asmString) {
        setStringValue(asmString);
    }

    private void setStringValue(String s) {
        asmString = "";
        boolean prevIsBS = false;
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            switch(charArray[i]) {
                case '\\': case '\"':
                    if (prevIsBS) {
                        asmString += charArray[i];
                        prevIsBS = false;
                    } else {
                        prevIsBS = true;
                    }
                    break;
                default:
                    prevIsBS = false;
                    asmString += charArray[i];
                    break;
            }
        }
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
    }

    @Override
    protected void verifyMethodBody(DecacCompiler compiler, EnvironmentExp envExp, 
        EnvironmentExp envExpMethod, ClassDefinition currentClass, Type tType) 
        throws ContextualError {

    }

    @Override
    protected ListDeclVar getDeclVars() {
        return null;
    }

    protected void codeGenInst(DecacCompiler compiler, ClassDefinition currentClass, int numDeclared,
        AbstractIdentifier methodName) {
        MethodDefinition methodDef = methodName.getMethodDefinition();
        compiler.addLabel(methodDef.getLabel());
        compiler.add(new InlinePortion(asmString));
    }

    @Override
    protected void byteGenInst(DecacCompiler compiler, ClassDefinition currentClass, 
            MethodVisitor mw, int numParams) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("asm(\"");
        char[] charArray = this.asmString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            switch(charArray[i]) {
                case '\\': case '\"':
                    s.print("\\");
                    break;
                default:
                    break;
            }
            s.print(charArray[i]);
        }
        s.println("\");");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // nothing to do here : no child
    }
}
