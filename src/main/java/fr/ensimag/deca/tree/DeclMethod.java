package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.MethodDefinition;
import fr.ensimag.deca.context.Signature;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.instructions.ERROR;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class DeclMethod extends AbstractDeclMethod {
    
    private AbstractIdentifier type;
    private AbstractIdentifier methodName;
    private ListDeclParam params;
    private AbstractMethodBody body;
    

    public DeclMethod(AbstractIdentifier type, AbstractIdentifier methodName,
                      ListDeclParam params, AbstractMethodBody body) {

        Validate.notNull(type);
        Validate.notNull(methodName);
        Validate.notNull(params);
        Validate.notNull(body);
        this.type = type;
        this.methodName = methodName;
        this.params = params;
        this.body = body;

    }
    
    public AbstractIdentifier getMethodName() {
        return this.methodName;
    }

    @Override
    protected void verifyDeclMethod(DecacCompiler compiler,
                    ClassDefinition superClass, ClassDefinition currentClass)
            throws ContextualError {

                EnvironmentExp envExp = currentClass.getMembers();
                Type tType = type.verifyType(compiler);
                methodName.resetName(compiler.getSymbolTable());
                Signature sig = params.verifyListParams(compiler);
                ExpDefinition expDefSup = envExp.get(methodName.getName());
                MethodDefinition methodDef;

                if (((expDefSup) != null) && expDefSup.isMethod()) {

                    if (!(sig.getArgs().equals(((MethodDefinition) expDefSup).getSignature().getArgs()))){
                        throw new ContextualError("Signature is different " +
                                             "from the one declared here: " +
                                             expDefSup.getLocation() +
                                             " (rule 2.7)",
                                             params.getLocation());
                    }

                    if (!(tType.sameType(expDefSup.getType()))) {
                        throw new ContextualError("The return type is not a " +
                                      "subType of the method declared here: " +
                                      expDefSup.getLocation() + " (rule 2.7)",
                                      params.getLocation());
                    }

                    methodDef = new MethodDefinition(tType,
                                    getLocation(), sig, 
                                    ((MethodDefinition) expDefSup).getIndex());

                } else {
                    methodDef = new MethodDefinition(tType,
                                        getLocation(), sig, 
                                        currentClass.incNumberOfMethods() - 1);
                }
                methodName.setDefinition(methodDef);
                methodName.setType(tType);

                try {
                    envExp.declare(methodName.getName(), methodDef);
                } catch (EnvironmentExp.DoubleDefException e) {
                    throw new ContextualError(methodName.getName() +
                                    " was already declared here: " +
                                    expDefSup.getLocation() + " (rule 2.7)",
                                    this.getLocation());
                }

    }

    @Override
    protected void verifyDeclMethod_pass3(DecacCompiler compiler,
                            EnvironmentExp envExp, ClassDefinition currentClass)
            throws ContextualError {

            Type tType = type.verifyType(compiler);
            EnvironmentExp envExpMethod = new EnvironmentExp(currentClass.getMembers());
            envExpMethod.setLabel(new Label(methodName.getName().toString()));
            params.verifyListParams_pass3(compiler, envExpMethod);
            body.verifyMethodBody(compiler, envExp, envExpMethod, currentClass, tType);
            methodName.getMethodDefinition().setNbLocals(envExpMethod.size() - methodName.getMethodDefinition().getSignature().size());

        }

    @Override
    public void codeGenDeclMethod(DecacCompiler compiler,
                                ClassDefinition currentClass, int numDeclared) {
        body.codeGenInst(compiler, currentClass, numDeclared, methodName);
    }

    @Override
    public void byteGenDeclMethod(DecacCompiler compiler, ClassDefinition currentClass, ClassWriter cw) {
        String desc = params.getByteDesc();
        if (type.getType().isVoid()) {
            desc += "V";
        } else if (type.getType().isBoolean()) {
            desc += "Z";
        } else if (type.getType().isClass()) {
            desc += "L";
            if (type.getType().getName().toString().equals("Object"))
                desc += "java/lang/Object";
            else
                desc += type.getType().getName().toString();
            desc += ";";
        } else {
            desc += "I";
        }
        methodName.getMethodDefinition().getSignature().setDescriptor(desc);
        // creates a MethodWriter for the method
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC , methodName.getName().toString(),
            desc, null, null);

        body.byteGenInst(compiler, currentClass, mw, methodName.getMethodDefinition().getSignature().size() + 1);

        if(type.getType().isVoid()) {
            mw.visitInsn(Opcodes.RETURN);
        } else {
            mw.visitInsn(Opcodes.NOP);
        }

        mw.visitMaxs(0, 0);
        mw.visitEnd();
        
    }

    
    @Override
    public void decompile(IndentPrintStream s) {
        type.decompile(s);
        s.print(" ");
        methodName.decompile(s);
        s.print("(");
        params.decompile(s);
        s.println(") ");
        body.decompile(s);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        methodName.iter(f);
        params.iter(f);
        body.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        methodName.prettyPrint(s, prefix, false);
        params.prettyPrint(s, prefix, false);
        body.prettyPrint(s, prefix, true);
    }

}
