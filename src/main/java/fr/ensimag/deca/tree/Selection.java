package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tools.IndentPrintStream;
import fr.ensimag.deca.context.Type;
import java.io.PrintStream;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ClassType;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.FieldDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.NullOperand;
import fr.ensimag.ima.pseudocode.instructions.BEQ;
import fr.ensimag.ima.pseudocode.instructions.CMP;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.LEA;
import fr.ensimag.ima.pseudocode.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Selection extends AbstractLValue {

        AbstractExpr lSelection;
        AbstractIdentifier identifier;

        public Selection(AbstractExpr lSelection, AbstractIdentifier identifier) {
            this.lSelection = lSelection;
            this.identifier = identifier;
        }

        @Override
        public Type verifyExpr(DecacCompiler compiler,
            EnvironmentExp localEnv, ClassDefinition currentClass)
            throws ContextualError {
                
                EnvironmentType envType = compiler.getEnvType();

                Type lSelectionType = lSelection.verifyExpr(compiler, localEnv, currentClass);

                if (!(lSelectionType instanceof ClassType)) {
                    throw new ContextualError("Expected object got " + lSelectionType + 
                                            " (rule 3.65)", lSelection.getLocation());
                }

                ClassType classType = (ClassType) lSelectionType;
                ClassDefinition classDef = classType.getDefinition();
                EnvironmentExp envExp = classDef.getMembers();

                Type fieldType = identifier.verifyExpr(compiler, envExp, currentClass);

                FieldDefinition fieldDef = identifier.getFieldDefinition();
                Visibility visib = fieldDef.getVisibility();

                if (visib == Visibility.PROTECTED) {
                    if (currentClass == null) {
                        throw new ContextualError("Attribute " + identifier.getName() + " is not accessible (rule 3.66)",
                                            identifier.getLocation());
                    }
                    if (!(currentClass.getType().isSubClassOf(classType))) {
                        throw new ContextualError("Attribute " + identifier.getName() + " is not accessible (rule 3.66)",
                                            identifier.getLocation());
                    }   
                    if (!(fieldDef.getContainingClass().getType().isSubClassOf(currentClass.getType()))) {
                        throw new ContextualError("Attribute " + identifier.getName() + " is not accessible (rule 3.66)",
                                            identifier.getLocation());
                    }

                }
                
                this.setType(fieldType);
                return fieldType;


        };

        @Override
        protected void codeGenExp(DecacCompiler compiler) {

            int index = this.codeGen(compiler);

            compiler.addInstruction(new LOAD(new RegisterOffset(index, Register.getR(2)), Register.getR(2)));
            compiler.addInstruction(new PUSH(Register.getR(2)));

        }

        @Override
        protected void codeGenAssign(DecacCompiler compiler) {

            int index = this.codeGen(compiler);

            compiler.addInstruction(new LEA(new RegisterOffset(index, Register.getR(2)), Register.getR(2)));
            compiler.addInstruction(new PUSH(Register.getR(2)));

        }

        private int codeGen(DecacCompiler compiler) {

            FieldDefinition fieldDef = this.identifier.getFieldDefinition();
            int index = fieldDef.getIndex();

            lSelection.codeGenExp(compiler);
            compiler.addInstruction(new POP(Register.getR(2)));
            compiler.addInstruction(new CMP(new NullOperand(), Register.getR(2)));
            compiler.addInstruction(new BEQ(new Label("dereferencement.null")));

            return index;

        }

        @Override
        protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {
            lSelection.byteGenExp(compiler, mw);
            String className = identifier.getFieldDefinition().getContainingClass().getType().getByteDescriptor();
            String name = identifier.getName().toString();
            String signature = identifier.getFieldDefinition().getType().getByteDescriptor();
            if (identifier.getFieldDefinition().getType().isClass())
                signature = "L" + signature + ";";
            mw.visitFieldInsn(Opcodes.GETFIELD, className, name, 
                signature);
        }

        protected void byteGenAssignPart1(MethodVisitor mw) {
            lSelection.byteGenExp(null, mw);
            mw.visitInsn(Opcodes.DUP);
        }

        protected void byteGenAssignPart2(MethodVisitor mw) {
            String className = identifier.getFieldDefinition().getContainingClass().getType().getByteDescriptor();
            String name = identifier.getName().toString();
            String signature = identifier.getFieldDefinition().getType().getByteDescriptor();
            if (identifier.getFieldDefinition().getType().isClass())
                signature = "L" + signature + ";";
            mw.visitFieldInsn(Opcodes.PUTFIELD, className, name, 
                signature);
            mw.visitFieldInsn(Opcodes.GETFIELD, className, name, 
                signature);
        }


        @Override
        protected void iterChildren(TreeFunction f) {
   
        }

        @Override
        protected void prettyPrintChildren(PrintStream s, String prefix) {
            lSelection.prettyPrint(s, prefix, false);
            identifier.prettyPrint(s, prefix, true);
        }

        @Override
        public void decompile(IndentPrintStream s) {
            lSelection.decompile(s);
            s.print(".");
            identifier.decompile(s);
        }

}
