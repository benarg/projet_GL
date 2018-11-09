package fr.ensimag.deca.context;

import fr.ensimag.deca.tools.SymbolTable.Symbol;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.deca.tree.Location;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.LabelOperand;

import java.util.HashMap;
import java.util.Map;

/**
 * Dictionary associating classes' TypeDefinition to their names.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class EnvironmentType {
    // A FAIRE : implémenter la structure de donnée représentant un
    // environnement (association nom -> définition, avec possibilité
    // d'empilement).

    private Map<Symbol, TypeDefinition> map;

    public EnvironmentType(SymbolTable symbolTable) {
        
        this.map =  new HashMap<Symbol, TypeDefinition>();
        
        // we add the predefined types to our newly created dictionary

        Symbol nullSymbol = symbolTable.create("null");
        TypeDefinition nullTypeDef = new ClassDefinition(new ClassType(nullSymbol, Location.BUILTIN, null), Location.BUILTIN, null);

        Symbol intSymbol = symbolTable.create("int");
        TypeDefinition intTypeDef = new TypeDefinition(new IntType(intSymbol), Location.BUILTIN);
        
        Symbol floatSymbol = symbolTable.create("float");
        TypeDefinition floatTypeDef = new TypeDefinition(new FloatType(floatSymbol), Location.BUILTIN); 
        
        Symbol booleanSymbol = symbolTable.create("boolean");
        TypeDefinition booleanTypeDef = new TypeDefinition(new BooleanType(booleanSymbol), Location.BUILTIN);
        
        Symbol voidSymbol = symbolTable.create("void");
        TypeDefinition voidTypeDef = new TypeDefinition(new VoidType(voidSymbol), Location.BUILTIN);
        
        Symbol objectSymbol = symbolTable.create("Object");
        ClassType objectType = new ClassType(objectSymbol, Location.BUILTIN, null);
        ClassDefinition objectClassDef = objectType.getDefinition();
        EnvironmentExp objectClassEnv = objectClassDef.getMembers();
        Signature signatureEquals = new Signature();
        signatureEquals.add(objectType);
        signatureEquals.setDescriptor("(Ljava/lang/Object;)Z");

        MethodDefinition equalsMethodDef = new MethodDefinition(new BooleanType(symbolTable.create("boolean")),
                                                                Location.BUILTIN,
                                                                signatureEquals,
                                                                0);
        equalsMethodDef.setLabel(new Label("code.Object.equals"));
        objectClassDef.incNumberOfMethods();
        Symbol equalsSym = symbolTable.create("equals");
        LabelOperand[] objectLabelTable = {new LabelOperand(new Label("code.Object.equals"))};
        objectClassDef.setLabelOperandsMethodsTable(objectLabelTable);
        try {
            objectClassEnv.declare(equalsSym, equalsMethodDef);
        } catch (EnvironmentExp.DoubleDefException e) {}

        try {
            declare(intSymbol, intTypeDef);
            declare(floatSymbol, floatTypeDef);
            declare(booleanSymbol, booleanTypeDef);
            declare(voidSymbol, voidTypeDef);
            declare(objectSymbol, objectClassDef);
        } catch (DoubleDefException e) {}

    }

    public static class DoubleDefException extends Exception {
        private static final long serialVersionUID = -2733379901827316441L;
    }

    /**
     * Return the definition of the symbol in the environment, or null if the
     * symbol is undefined.
     */
    public TypeDefinition get(Symbol key) {
        return this.map.get(key);
    }

    /**
     * Add the definition def associated to the symbol name in the environment.
     * 
     * Adding a symbol which is already defined in the environment,
     * - throws DoubleDefException if the symbol is in the "current" dictionary 
     * - or, hides the previous declaration otherwise.
     * 
     * @param name
     *            Name of the symbol to define
     * @param def
     *            Definition of the symbol
     * @throws DoubleDefException
     *             if the symbol is already defined at the "current" dictionary
     *
     */
    public void declare(Symbol name, TypeDefinition def) throws DoubleDefException {
        if (this.map.containsKey(name))
            throw new DoubleDefException();
        this.map.put(name, def);
    }

}
