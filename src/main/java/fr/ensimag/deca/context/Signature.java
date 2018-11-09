package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl56
 * @date 01/01/2017
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();

    public void add(Type t) {
        args.add(t);
    }
    
    public Type paramNumber(int n) {
        return args.get(n);
    }
    
    public int size() {
        return args.size();
    }

    public List<Type> getArgs() {
        return this.args;
    }

    private String byteDescriptor = "()V";

    public void setDescriptor(String desc) {
        this.byteDescriptor = desc;
    }

    public String getDescriptor() {
        return this.byteDescriptor;
    }
    
}