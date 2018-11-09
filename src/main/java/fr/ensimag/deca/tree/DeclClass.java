package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.TypeDefinition;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import fr.ensimag.ima.pseudocode.instructions.LEA;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import org.apache.commons.lang.Validate;

/**
 * Declaration of a class (<code>class name extends superClass {members}<code>).
 * 
 * @author gl56
 * @date 01/01/2017
 */
public class DeclClass extends AbstractDeclClass {
    
    private AbstractIdentifier superClass;
    private ListDeclMethod methods;
    private ListDeclField fields;
    private AbstractIdentifier className;
    
    public DeclClass(AbstractIdentifier superClass, ListDeclMethod methods,
                     AbstractIdentifier className, ListDeclField fields) {
        Validate.notNull(superClass);
        Validate.notNull(className);
        Validate.notNull(methods);
        Validate.notNull(fields);
        this.methods = methods;
        this.className = className;
        this.superClass = superClass;
        this.fields = fields;
    }

    public AbstractIdentifier getSuperClass() {
        return this.superClass;
    }

    public AbstractIdentifier getClassName() {
        return this.className;
    }

    @Override
    public void decompile(IndentPrintStream s) {
        s.print("class ");
        className.decompile(s);
        s.print(" extends ");
        superClass.decompile(s);
        s.println(" {");
        s.indent();
        fields.decompile(s);
        methods.decompile(s);
        s.unindent();
        s.println("}"); 
    }

    @Override
    protected void verifyClass(DecacCompiler compiler) throws ContextualError {

        className.resetName(compiler.getSymbolTable());

        Type tSuperClass = superClass.verifyType(compiler);
        
        if (!tSuperClass.isClass()) {
            throw new ContextualError("Expected class type, got " + tSuperClass
                    + " (rule 2.3)", this.superClass.getLocation());
        }

        ClassType tClass = new ClassType(className.getName(),
                                         className.getLocation(),
                                         superClass.getClassDefinition());
        ClassDefinition defClass = tClass.getDefinition();
        
        className.setDefinition(defClass);
        superClass.setDefinition(compiler.getEnvType().get(superClass.getName()));

        try {
            compiler.getEnvType().declare(className.getName(), defClass);
        } catch (EnvironmentType.DoubleDefException e) {
            throw new ContextualError(className.getName() +
                " was already declared here: " + 
                compiler.getEnvType().get(className.getName()).getLocation() +
                " (rule 1.3)", this.getLocation());
        }

    }

    @Override
    protected void verifyClassMembers(DecacCompiler compiler)
            throws ContextualError {

        ClassDefinition superClass = getSuperClass().getClassDefinition();
        ClassDefinition currentClass = getClassName().getClassDefinition();
        currentClass.setNumberOfFields(superClass.getNumberOfFields());
        currentClass.setNumberOfMethods(superClass.getNumberOfMethods());

        fields.verifyListField(compiler, superClass, currentClass);
        methods.verifyListMethod(compiler, superClass, currentClass);

    }
    

    @Override
    protected void verifyClassBody(DecacCompiler compiler)
            throws ContextualError {
        
        EnvironmentType envType = compiler.getEnvType();
        TypeDefinition typeDef = envType.get(className.getName());

        if (!(typeDef instanceof ClassDefinition)) {
            throw new DecacInternalError("ClassDef should be ClassDefinition");
        }

        ClassDefinition classDef = (ClassDefinition) typeDef;
        EnvironmentExp envExp = classDef.getMembers(); 

        fields.verifyListField_pass3(compiler, envExp, classDef);
        methods.verifyListMethod_pass3(compiler, envExp, classDef);

    }

    @Override
    public void codeGenDeclClass(DecacCompiler compiler) {

        ClassDefinition classDef = className.getClassDefinition();
        ClassDefinition superClassDef = classDef.getSuperClass();
        classDef.setOperand(new RegisterOffset(compiler.getVariableGlobales()+1,
                                               Register.GB));
        
        compiler.addComment("Code de la table des méthodes de " +
                            classDef.getType().getName());
        compiler.addInstruction(new LEA(superClassDef.getOperand(),
                                        Register.getR(0)));
        compiler.addInstruction(new STORE(Register.getR(0),
                                          classDef.getOperand()));
        
        compiler.addVariableGlobales(1);
        methods.codeGenMethodsTable(compiler, classDef);

    }

    @Override
    public void codeGenClassMethods(DecacCompiler compiler) {
        compiler.addComment("-------" + className.getName() + "-------");
        fields.codeGenListField(compiler, className.getClassDefinition(),
                        superClass.getClassDefinition().getNumberOfFields());
        methods.codeGenListMethod(compiler, className.getClassDefinition());
    } 

    @Override
    public void byteGenDeclClass(DecacCompiler compiler) {
        // creates a ClassWriter for the class
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        String superName = superClass.getName().toString();
        if (superName.equals("Object"))
            superName = "java/lang/Object";
        // which inherits superClass
        cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, className.getName().toString(), null, superName, null);

        //_________Constructor_________

        // creates a MethodWriter for the (implicit) constructor
        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null,
                null);
        // pushes the 'this' variable
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        // invokes the super class constructor
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL, superName, "<init>", "()V",
                false);

        fields.byteGenListField(cw, mw);

        mw.visitInsn(Opcodes.RETURN);
        // these values are ignored
        mw.visitMaxs(0, 0);
        mw.visitEnd();
        //______________________________

        methods.byteGenListMethods(compiler, className.getClassDefinition(), cw);

        compiler.addClass(className.getName().toString(), cw);

        /*
        byte[] code = cw.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(className.getName() + ".class");
            fos.write(code);
            fos.close();
        } catch (IOException e) {
            System.out.println(e);
        } */
    }

    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        className.prettyPrint(s, prefix, false);
        superClass.prettyPrint(s, prefix, false);
        fields.prettyPrint(s, prefix, false);
        methods.prettyPrint(s, prefix, true);
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        superClass.iter(f);
        methods.iter(f);
        fields.iter(f);
        className.iter(f);
    }

}
