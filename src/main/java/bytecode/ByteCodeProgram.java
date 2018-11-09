package bytecode;

import fr.ensimag.deca.DecacFatalError;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;

/*
 * Abstract representation of Byte Codes
 *
 * @author Ensimag, Groupe GL56
 * @date 13/01/2017
 */
public class ByteCodeProgram {

    public ByteCodeProgram(String className) {
        this.cw.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, className, null, "java/lang/Object", null);
        this.putCW(className, cw);
    }

    // Flag to automatically compute the maximum stack size 
    // and the maximum number of local variables of methods
    private final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

    /**
     * getter of the ClassWriter
     */
    public ClassWriter getCW() {
        return this.cw;
    }

    /**
     * returns the bytecode of the Main class, and loads it dynamically
     */
    public byte[] getByteCode() {
        return cw.toByteArray();
    }

    /**
     * Map containing all the classes to generate
     */
    private Map<String, ClassWriter> classWriters = new HashMap<String, ClassWriter>();

    /**
     * adds a class and it's corresponding classWriter in the Map
     */
    public void putCW(String className, ClassWriter cw) {
        this.classWriters.put(className, cw);
    }

    /**
     * 
     */
    public void generateFiles(String destPath) throws DecacFatalError {
        for (String className : this.classWriters.keySet()) {
            try {
                byte [] code = this.classWriters.get(className).toByteArray();
                FileOutputStream fos = new FileOutputStream(destPath + className  + ".class");
                fos.write(code);
                fos.close();
            } catch (IOException e) {
                throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
            }
        }
    }
}
