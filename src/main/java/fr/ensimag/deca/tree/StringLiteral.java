package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.StringType;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.ima.pseudocode.ImmediateString;
import fr.ensimag.ima.pseudocode.instructions.WSTR;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;
import org.objectweb.asm.MethodVisitor;


/**
 * String literal
 *
 * @author gl56
 * @date 01/01/2017
 */
public class StringLiteral extends AbstractStringLiteral {

    @Override
    public String getValue() {
        return value;
    }

    private String value;

    public StringLiteral(String value) {
        Validate.notNull(value);
        setStringValue(value);
    }

    private void setStringValue(String s) {
        value = "";
        boolean prevIsBS = false;
        char[] charArray = s.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            switch(charArray[i]) {
                case '\\': case '\"':
                    if (prevIsBS) {
                        value += charArray[i];
                        prevIsBS = false;
                    } else {
                        prevIsBS = true;
                    }
                    break;
                default:
                    value += charArray[i];
                    break;
            }
        }
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {

	Symbol stringSymbol = compiler.getSymbolTable().create("string");
    StringType thisType = new StringType(stringSymbol);
    setType(thisType);
	    
	return thisType;
	
    }

    @Override
    protected void codeGenPrint(DecacCompiler compiler) {
        compiler.addInstruction(new WSTR(new ImmediateString(value)));
    }

    @Override
    protected void byteGenPrint(DecacCompiler compiler, MethodVisitor mw) {
        // pushes the string value
        mw.visitLdcInsn(this.value);
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("\"");
        char[] charArray = this.value.toCharArray();
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
        s.print("\"");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        // leaf node => nothing to do
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        // leaf node => nothing to do
    }
    
    @Override
    String prettyPrintNode() {
        return "StringLiteral (" + value + ")";
    }

}
