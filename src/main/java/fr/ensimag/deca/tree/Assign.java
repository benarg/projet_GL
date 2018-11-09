package fr.ensimag.deca.tree;

import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.context.Type;
import fr.ensimag.deca.DecacCompiler;
import fr.ensimag.deca.context.ExpDefinition;
import fr.ensimag.deca.context.ClassDefinition;
import fr.ensimag.deca.context.ContextualError;
import fr.ensimag.deca.context.Definition;
import fr.ensimag.deca.context.EnvironmentExp;
import fr.ensimag.ima.pseudocode.Register;
import fr.ensimag.ima.pseudocode.RegisterOffset;
import fr.ensimag.ima.pseudocode.instructions.LOAD;
import fr.ensimag.ima.pseudocode.instructions.POP;
import fr.ensimag.ima.pseudocode.instructions.PUSH;
import fr.ensimag.ima.pseudocode.instructions.STORE;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


/**
 * Assignment, i.e. lvalue = expr.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Assign extends AbstractBinaryExpr {

    @Override
    public AbstractLValue getLeftOperand() {
        // The cast succeeds by construction, as the leftOperand has been set
        // as an AbstractLValue by the constructor.
        return (AbstractLValue)super.getLeftOperand();
    }

    public Assign(AbstractLValue leftOperand, AbstractExpr rightOperand) {
        super(leftOperand, rightOperand);
    }

    @Override
    public Type verifyExpr(DecacCompiler compiler, EnvironmentExp localEnv,
            ClassDefinition currentClass) throws ContextualError {
        
                AbstractLValue leftOp = this.getLeftOperand();
                AbstractExpr rightOp = this.getRightOperand();
                
                Type leftType = leftOp.verifyLValue(compiler, localEnv, currentClass);

                setRightOperand(rightOp.verifyRValue(compiler, localEnv, currentClass, leftType));
                setType(leftType);

                return leftType;

    }

    @Override
    protected void codeGenExp(DecacCompiler compiler) {

        // Puts onto the stack the value of the expression to assign
        getRightOperand().codeGenExp(compiler);

        try {

            /*
             * if it's an abstract identifier, we fetch the address of the
             * variable on the stack
             */ 
            AbstractIdentifier ident = (AbstractIdentifier) getLeftOperand();

            if (ident.getDefinition().isField()) {
                Selection s = new Selection(new This(), ident);
                s.codeGenAssign(compiler);
                compiler.addInstruction(new POP(Register.getR(3)));
                compiler.addInstruction(new POP(Register.getR(2)));
                compiler.addInstruction(new STORE(Register.getR(2), new RegisterOffset(0, Register.getR(3))));
            } else {
                compiler.addInstruction(new POP(Register.getR(2)));
                compiler.addInstruction(new STORE(Register.getR(2), ident.getExpDefinition().getOperand()));
            }

        } catch (ClassCastException e) {

            /*
             * if it's not an abstract identifier, it is then a selection, so
             * we fetch the address of the field with the codeGenAssign
             */
            getLeftOperand().codeGenAssign(compiler);
            compiler.addInstruction(new POP(Register.getR(3)));
            compiler.addInstruction(new POP(Register.getR(2)));
            compiler.addInstruction(new STORE(Register.getR(2), new RegisterOffset(0, Register.getR(3))));

        }

        compiler.addInstruction(new PUSH(Register.getR(2)));

    }

    @Override
    protected void byteGenExp(DecacCompiler compiler, MethodVisitor mw) {

        // Works the same way as the method above
        Selection sel = null;
        
        try {
            AbstractIdentifier ident = (AbstractIdentifier) getLeftOperand();
            ExpDefinition expDef = ident.getExpDefinition();
            if (expDef.isField()) {
                sel = new Selection(new This(), ident);
            } else {
                getRightOperand().byteGenExp(compiler, mw);
                Type typeVar = expDef.getType();
                int id = expDef.getId();

                if (typeVar.isFloat()) {
                    mw.visitVarInsn(Opcodes.FSTORE,id); 
                    mw.visitVarInsn(Opcodes.FLOAD,id);
                } else if(typeVar.isClass()){
                    mw.visitVarInsn(Opcodes.ASTORE,id);
                    mw.visitVarInsn(Opcodes.ALOAD,id);
                } else {
                    mw.visitVarInsn(Opcodes.ISTORE,id);
                    mw.visitVarInsn(Opcodes.ILOAD,id);
                }
            }

        } catch (ClassCastException e) {
            /*
             * if it's not an abstract identifier, it is then a selection
             */
            sel = (Selection) getLeftOperand();
        }
        if (sel != null) {
            sel.byteGenAssignPart1(mw);
            getRightOperand().byteGenExp(compiler, mw);
            sel.byteGenAssignPart2(mw);
        }  
    }

    @Override
    protected String getOperatorName() {
        return "=";
    }

}
