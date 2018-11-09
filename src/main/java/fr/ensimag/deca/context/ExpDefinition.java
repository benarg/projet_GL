package fr.ensimag.deca.context;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.DAddr;

/**
 * Definition associated to identifier in expressions.
 *
 * @author gl56
 * @date 01/01/2017
 */
public abstract class ExpDefinition extends Definition {

    public void setOperand(DAddr operand) {
        this.operand = operand;
    }

    public DAddr getOperand() {
        return operand;
    }
    private DAddr operand;
    private int id;

    public void setId(int ident) {
        this.id = ident;
    }

    public int getId() {
        return this.id;
    }


    public ExpDefinition(Type type, Location location) {
        super(type, location);
    }

}
