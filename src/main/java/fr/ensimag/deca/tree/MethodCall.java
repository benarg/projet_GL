package fr.ensimag.deca.tree;

import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.SUBSP;
import fr.ensimag.ima.pseudocode.instructions.BSR;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Iterator;
import java.util.List;
/**
 *
 * @author gl56
 */
public class MethodCall extends AbstractExpr {
    
    private AbstractIdentifier methodName;
    private AbstractExpr varName;
    private ListExpr arguments;
    
    public MethodCall(AbstractExpr varName, AbstractIdentifier methodName, ListExpr arguments) {
        Validate.notNull(varName);
        Validate.notNull(methodName);
        Validate.notNull(arguments);
        this.varName = varName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv, ClassDefinition currentClass) 
        throws ContextualError {
        


            Type varType = varName.verifyExpr(compiler, localEnv, currentClass);

            
            // we verify if the variable which call the method is an object
            if (!(varType.isClass())) {
                throw new ContextualError("Expected Object got : " + varType +
                    " (rule 3.71)", varName.getLocation());
            }

            ClassType classType = (ClassType) varType;
            ClassDefinition classDefinition = (ClassDefinition) classType.getDefinition();
            EnvironmentExp classEnv = classDefinition.getMembers();

            
            Type methodReturnType = methodName.verifyExpr(compiler, classEnv, currentClass);
            MethodDefinition methodDef = (MethodDefinition) classEnv.get(methodName.getName());

            Signature methodSig = methodDef.getSignature();
            List<Type> methodVarTypes = methodSig.getArgs();

            // verify the type of the arguments
            arguments.verifyListExpr(compiler, localEnv, currentClass, methodVarTypes);

            this.setType(methodReturnType);

            return methodReturnType;

    }

    @Override
    public void decompile(IndentPrintStream s) {
        varName.decompile(s);
        s.print(".");
        methodName.decompile(s);
        s.print("(");
        arguments.decompile(s);
        s.print(")");
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        varName.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        arguments.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        varName.iter(f);
        methodName.iter(f);
        arguments.iter(f);
    }

    // push the parameters in the stack
    private void paramCodeGenCall(Iterator<AbstractExpr> ite, DecacCompiler compiler) {
        if (ite.hasNext()){
            AbstractExpr a = ite.next();
            paramCodeGenCall(ite, compiler);
            a.codeGenExp(compiler);
        }
    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {

        MethodDefinition methodDef = methodName.getMethodDefinition();
        int index = methodDef.getIndex();

        Iterator<AbstractExpr> ite = arguments.iterator();
        
        paramCodeGenCall(ite, compiler); // stock the arguments
        
        varName.codeGenExp(compiler); // stock the variables

        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.SP), Register.getR(2)));
        compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));
        compiler.addInstruction(new BEQ(new Label("dereferencement.null")));
        compiler.addInstruction(new LOAD(new RegisterOffset(0, Register.getR(2)), Register.getR(3)));
        compiler.addInstruction(new LOAD(new RegisterOffset(index + 1, Register.getR(3)), Register.getR(3)));
        compiler.addInstruction(new BSR(Register.getR(3)));
        compiler.addInstruction(new SUBSP(1 + methodDef.getSignature().size()));
        if (!(methodDef.getType().isVoid())) {
            compiler.addInstruction(new PUSH(Register.getR(0)));
        }
    } 

    // push the parameters in the stack
    private void paramByteGenCall(Iterator<AbstractExpr> ite, DecacCompiler compiler, MethodVisitor mw) {
        while (ite.hasNext()){
            AbstractExpr a = ite.next();
            a.byteGenExp(compiler, mw);
        }
    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
        varName.byteGenExp(compiler, mw);

        Iterator<AbstractExpr> ite = arguments.iterator();
        paramByteGenCall(ite, compiler, mw);
        
        String className = varName.getType().toString();
        String descriptor = methodName.getMethodDefinition().getSignature().getDescriptor();
        /*if (methodName.getMethodDefinition().getType().isClass())
            descriptor = "L" + descriptor + ";";*/
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className, methodName.getName().toString(), 
            descriptor, false);
    }

}
