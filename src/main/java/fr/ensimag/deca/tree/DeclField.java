package fr.ensimag.deca.tree;

import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.ima.pseudocode.ImmediateFloat;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import java.io.PrintStream;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * @author gl56
 * @date 01/01/2017
 */
public class DeclField extends AbstractDeclField {

    final private Visibility visibility;
    final private AbstractIdentifier type;
    final private AbstractIdentifier fieldName;
    final private AbstractInitialization initialization;

    public DeclField(Visibility visibility,  AbstractIdentifier type, 
                     AbstractIdentifier fieldName,
                     AbstractInitialization initialization) {

        this.visibility = visibility;
        this.type = type;
        this.fieldName = fieldName;
        this.initialization = initialization;

    }

    @Override
    protected void verifyDeclField(DecacCompiler compiler,
                ClassDefinition superClass, ClassDefinition currentClass) 
                throws ContextualError {

        fieldName.resetName(compiler.getSymbolTable());

        Type typeOfField = type.verifyType(compiler);
        if (typeOfField.isVoid()) {
            throw new ContextualError("Cannot have void type field (rule 2.5)",
                                      type.getLocation());
        }

        EnvironmentType envType = compiler.getEnvType();
        EnvironmentExp envExp = currentClass.getMembers();
        int lastIndex = currentClass.getNumberOfFields();
        FieldDefinition fieldDef = new FieldDefinition(typeOfField,
                                                       this.getLocation(),
                                                       this.visibility,
                                                       currentClass,
                                                       lastIndex + 1);

        currentClass.incNumberOfFields();

        try {
            envExp.declare(fieldName.getName(), fieldDef);
        } catch (EnvironmentExp.DoubleDefException e) {
            throw new ContextualError(fieldName.getName() +
                                " was already declared here: " + 
                                envExp.get(fieldName.getName()).getLocation() +
                                " (rule 3.17)", this.getLocation());
        }
        this.fieldName.setDefinition(fieldDef);

    }

    @Override
    protected void verifyDeclField_pass3(DecacCompiler compiler,
                        EnvironmentExp envExp, ClassDefinition currentClass)
            throws ContextualError{

            Type typeOfField = type.verifyType(compiler);

            initialization.verifyInitialization(compiler, typeOfField,
                                                currentClass.getMembers(),
                                                currentClass);

    }
    
    @Override
    public void codeGenNoInitialization(DecacCompiler compiler) {

        compiler.addComment("Initialisation de l'attribut " +
                            fieldName.getName());

        Type typeOfField = type.getType();
        
        if (typeOfField.isFloat() ) {
            compiler.addInstruction(new LOAD(new ImmediateFloat(0),
                                             Register.getR(0)));
        } else if(typeOfField.isClass()) {
            compiler.addInstruction(new LOAD(new NullOperand(),
                                             Register.getR(0)));
        } else {
            compiler.addInstruction(new LOAD(0, Register.getR(0)));
        }

    }
    
    @Override
    public void codeGenInitialization(DecacCompiler compiler, int offsetField) {

        if (initialization instanceof NoInitialization) {

            codeGenNoInitialization(compiler);
            compiler.addInstruction(new STORE(Register.getR(0),
                                new RegisterOffset(offsetField , Register.R1)));

        } else {

            compiler.addComment("Initialisation de l'attribut " +
                                fieldName.getName());
            compiler.addInstruction(new PUSH(Register.getR(1)));
            this.initialization.codeGenInitialization(compiler,
                                            new RegisterOffset(1, Register.SP));
            compiler.addInstruction(new LOAD(new RegisterOffset(1,Register.SP),
                                                            Register.getR(2)));
            compiler.addInstruction(new POP(Register.getR(1)));
            compiler.addInstruction(new STORE(Register.getR(2),
                            new RegisterOffset(offsetField, Register.getR(1))));

        }

    }

    @Override
    protected void byteGenDeclField(ClassWriter currentCW, MethodVisitor initMW){
        FieldVisitor fv;
        String descriptor = type.getType().getByteDescriptor();
        if (type.getType().isClass()) {
            descriptor = "L" + descriptor + ";";
        }
        if (visibility == Visibility.PUBLIC) {
            fv = currentCW.visitField(Opcodes.ACC_PUBLIC, 
                fieldName.getName().toString(), 
                descriptor, null, null);
        } else {
            fv = currentCW.visitField(Opcodes.ACC_PROTECTED, 
                fieldName.getName().toString(), 
                descriptor, null, null);
        }
        fv.visitEnd();
        initialization.byteGenInitField(fieldName, initMW);
    }
    
    @Override
    public void decompile(IndentPrintStream s) {
        if (visibility  == Visibility.PROTECTED) {
            s.print("protected ");
        }
        type.decompile(s);
        s.print(" ");
        fieldName.decompile(s);
        initialization.decompile(s);
        s.print(";");
    }

    @Override
    protected void iterChildren(TreeFunction f) {
        type.iter(f);
        fieldName.iter(f);
        initialization.iter(f);
    }
    
    @Override
    protected void prettyPrintChildren(PrintStream s, String prefix) {
        type.prettyPrint(s, prefix, false);
        fieldName.prettyPrint(s, prefix, false);
        initialization.prettyPrint(s, prefix, true);
    }

    @Override
    String prettyPrintNode() {
        return "[Visibility=" + this.visibility +"] " + super.prettyPrintNode();   
    } 
}
