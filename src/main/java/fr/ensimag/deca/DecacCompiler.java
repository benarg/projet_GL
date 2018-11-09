package fr.ensimag.deca;

import fr.ensimag.deca.syntax.DecaLexer;
import fr.ensimag.deca.syntax.DecaParser;
import fr.ensimag.deca.context.EnvironmentType;
import fr.ensimag.deca.tools.DecacInternalError;
import fr.ensimag.deca.tree.AbstractProgram;
import fr.ensimag.deca.tree.LocationException;
import fr.ensimag.deca.tools.SymbolTable;
import fr.ensimag.ima.pseudocode.AbstractLine;
import fr.ensimag.ima.pseudocode.IMAProgram;
import fr.ensimag.ima.pseudocode.Instruction;
import fr.ensimag.ima.pseudocode.Label;
import fr.ensimag.ima.pseudocode.Line;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.concurrent.*;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.log4j.Logger;
import org.objectweb.asm.ClassWriter;
import bytecode.ByteCodeProgram;

/**
 * Decac compiler instance.
 *
 * This class is to be instantiated once per source file to be compiled. It
 * contains the meta-data used for compiling (source file name, compilation
 * options) and the necessary utilities for compilation (symbol tables, abstract
 * representation of target file, ...).
 *
 * It contains several objects specialized for different tasks. Delegate methods
 * are used to simplify the code of the caller (e.g. call
 * compiler.addInstruction() instead of compiler.getProgram().addInstruction()).
 *
 * @author gl56
 * @date 01/01/2017
 */
public class DecacCompiler {

    /**
     * The symbolTable is valid for the entire source file.
     */
    private SymbolTable symbolTable;

    /**
     * The EnvType is the global environment for the entire program
     */
    private EnvironmentType envType;
    
    /**
     * used to determine TSTO
     */
    private Integer variableTemporaires;

    /**
     * used to determine TSTO
     */
    private Integer variableTemporaireMax;

    /**
     * used to determine ADDSP
     */
    private Integer variableGlobales;
    
    public SymbolTable getSymbolTable() {

	    return this.symbolTable;

    }

    public EnvironmentType getEnvType() {

        return this.envType;

    }
    
    
    private static final Logger LOG = Logger.getLogger(DecacCompiler.class);
    
    /**
     * Portable newline character.
     */
    private static final String nl = System.getProperty("line.separator", "\n");

    public DecacCompiler(CompilerOptions compilerOptions, File source) {
        super();
        this.compilerOptions = compilerOptions;
        this.source = source;

	    this.symbolTable = new SymbolTable();
        this.envType = new EnvironmentType(this.symbolTable);
        this.variableTemporaireMax = 0;
        this.variableTemporaires = 0;
        this.variableGlobales = 0;
    }

    public Integer getVariableTemporaires() {
        return this.variableTemporaires;
    }
    
    public Integer getVariableTemporaireMax() {
        return this.variableTemporaireMax;
    }
    
    public void setIncrementeVariableTemporaires(Integer i) {
        this.variableTemporaires += i;
    }
    
    public void setVariableTemporaireMax(Integer i) {
        this.variableTemporaireMax = i;
    }
    
    public Integer getVariableGlobales() {
        return this.variableGlobales;
    }
    
    public void setVariableGlobales(Integer i) {
        this.variableGlobales = i;
    }

    public void addVariableGlobales(Integer i) {
        this.variableGlobales += i;
    }
    
    /**
     * Source file associated with this compiler instance.
     */
    public File getSource() {
        return source;
    }

    /**
     * Compilation options (e.g. when to stop compilation, number of registers
     * to use, ...).
     */
    public CompilerOptions getCompilerOptions() {
        return compilerOptions;
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#add(fr.ensimag.ima.pseudocode.AbstractLine)
     */
    public void add(AbstractLine line) {
        program.add(line);
    }

    /**
     * @see fr.ensimag.ima.pseudocode.IMAProgram#addComment(java.lang.String)
     */
    public void addComment(String comment) {
        program.addComment(comment);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addLabel(fr.ensimag.ima.pseudocode.Label)
     */
    public void addLabel(Label label) {
        program.addLabel(label);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction)
     */
    public void addInstruction(Instruction instruction) {
        program.addInstruction(instruction);
    }

    /**
     * @see
     * fr.ensimag.ima.pseudocode.IMAProgram#addInstruction(fr.ensimag.ima.pseudocode.Instruction,
     * java.lang.String)
     */
    public void addInstruction(Instruction instruction, String comment) {
        program.addInstruction(instruction, comment);
    }

    /**
     * gets the ClassWriter of the ByteCodeProgram
     */
    public ClassWriter getCW(){
        return byteProgram.getCW();
    }


    /**
     * adds a class and it's corresponding classWriter in the 
     * DataStructure controlling the classes
     */
    public void addClass(String className, ClassWriter cw) {
        this.byteProgram.putCW(className, cw);
    }
    
    /**
     * @see 
     * fr.ensimag.ima.pseudocode.IMAProgram#display()
     */
    public String displayIMAProgram() {
        return program.display();
    }
    
    private final CompilerOptions compilerOptions;
    private final File source;
    /**
     * The main program. Every instruction generated will eventually end up here.
     */
    private final IMAProgram program = new IMAProgram();

    private ByteCodeProgram byteProgram;
    
    public void addFirst(Line l) {

        this.program.addFirst(l);
        
    }
 
    /**
     * Callable Class that wraps the compile() method of a DecacCompiler
     */
    private static class CompilerCallable implements Callable<Boolean> {

        public CompilerCallable(CompilerOptions options, File sourceFile){
            this.compiler = new DecacCompiler(options, sourceFile);;
            this.decaFile = sourceFile;
        }

        private DecacCompiler compiler;
        private File decaFile;

        @Override
        public Boolean call() throws Exception
        {
            boolean error = compiler.compile();
            return error;
        }

    }

    /**
     * Static method that creates a ThreadPool to compile all the files in parallel
     * 
     * @param  options [compiler's options]
     * @return error   [true on error]
     */
    public static boolean compileParallel(CompilerOptions options) {
        boolean error = false;
        List<CompilerCallable> compilers = new ArrayList<CompilerCallable>();
        for (File source : options.getSourceFiles()) {

            //creates a callable for each DecacCompiler's method compile()
            CompilerCallable callable = new CompilerCallable(options, source);

            //adds the callable to the list
            compilers.add(callable);
        }

        //creates a pool executor with compilers.length threads
        ExecutorService executor = Executors.newFixedThreadPool(compilers.size());

        try
        {
            //starts the threads
            List<Future<Boolean>> futureList = executor.invokeAll(compilers);

            for(Future<Boolean> voidFuture : futureList)
            {
                try {
                    error |= voidFuture.get();
                }
                catch (ExecutionException e) {
                    error = true;
                }
            }


        }
        catch (InterruptedException ie)
        {
           //do something if you care about interruption;
        }
        return error;

    }

    /**
     * Run the compiler (parse source file, generate code)
     *
     * @return true on error
     */
    public boolean compile() {
        String sourceFile = source.getAbsolutePath();
        //Trims .deca from sourceFile and adds ".ass" to obtain destFile
        String trimedFileName = sourceFile.substring(0, sourceFile.lastIndexOf('.'));
        byteProgram = new ByteCodeProgram(trimedFileName.substring(trimedFileName.lastIndexOf('/')+1, trimedFileName.length()));
        PrintStream err = System.err;
        PrintStream out = System.out;
        LOG.debug("Compiling file " + sourceFile 
            + " to assembly file " + trimedFileName + ".ass");
        try {
            return doCompile(sourceFile, trimedFileName, out, err);
        } catch (LocationException e) {
            e.display(err);
            return true;
        } catch (DecacFatalError e) {
            err.println(e.getMessage());
            return true;
        } catch (StackOverflowError e) {
            LOG.debug("stack overflow", e);
            err.println("Stack overflow while compiling file " + sourceFile + ".");
            return true;
        } catch (Exception e) {
            LOG.fatal("Exception raised while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        } catch (AssertionError e) {
            LOG.fatal("Assertion failed while compiling file " + sourceFile
                    + ":", e);
            err.println("Internal compiler error while compiling file " + sourceFile + ", sorry.");
            return true;
        }
    }

    /**
     * Internal function that does the job of compiling (i.e. calling lexer,
     * verification and code generation).
     *
     * @param sourceName name of the source (deca) file
     * @param destName name of the destination (assembly) file
     * @param out stream to use for standard output (output of decac -p)
     * @param err stream to use to display compilation errors
     *
     * @return true on error
     */
    private boolean doCompile(String sourceName, String destName,
            PrintStream out, PrintStream err)
            throws DecacFatalError, LocationException {
        AbstractProgram prog = doLexingAndParsing(sourceName, err);

        if (prog == null) {
            LOG.info("Parsing failed");
            return true;
        }
        assert(prog.checkAllLocations());

        if (getCompilerOptions().getParse()) {
            System.out.print(prog.decompile());
            return false;
        }


        prog.verifyProgram(this);
        assert(prog.checkAllDecorations());

        if (getCompilerOptions().getVerification()) {
            return false;
        }

        
        if (compilerOptions.getByteCode()) {
            prog.byteGenProgram(this);
            byte[] code = getCW().toByteArray();
            LOG.debug("Generated byte code");
            LOG.info("Output file bytecode file is: " + destName + ".class");
            this.byteProgram.generateFiles(destName.substring(0, destName.lastIndexOf('/')+1));
            /*try {
                FileOutputStream fos = new FileOutputStream(destName + ".class");
                fos.write(code);
                fos.close();
            } catch (IOException e) {
                throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
            }*/
        } else {
            addComment("start main program");
            prog.codeGenProgram(this);
            addComment("end main program");
            LOG.debug("Generated assembly code:" + nl + program.display());
            LOG.info("Output file assembly file is: " + destName + ".ass");


            FileOutputStream fstream = null;
            try {
                fstream = new FileOutputStream(destName + ".ass");
            } catch (FileNotFoundException e) {
                throw new DecacFatalError("Failed to open output file: " + e.getLocalizedMessage());
            }
            LOG.info("Writing assembler file ...");
            program.display(new PrintStream(fstream));
        }        
        LOG.info("Compilation of " + sourceName + " successful.");
        return false;
    }

    /**
     * Build and call the lexer and parser to build the primitive abstract
     * syntax tree.
     *
     * @param sourceName Name of the file to parse
     * @param err Stream to send error messages to
     * @return the abstract syntax tree
     * @throws DecacFatalError When an error prevented opening the source file
     * @throws DecacInternalError When an inconsistency was detected in the
     * compiler.
     * @throws LocationException When a compilation error (incorrect program)
     * occurs.
     */
    protected AbstractProgram doLexingAndParsing(String sourceName, PrintStream err)
            throws DecacFatalError, DecacInternalError {
        DecaLexer lex;
        try {
            lex = new DecaLexer(new ANTLRFileStream(sourceName));
        } catch (IOException ex) {
            throw new DecacFatalError("Failed to open input file: " + ex.getLocalizedMessage());
        }
        lex.setDecacCompiler(this);
        CommonTokenStream tokens = new CommonTokenStream(lex);
        DecaParser parser = new DecaParser(tokens);
        parser.setDecacCompiler(this);
        return parser.parseProgramAndManageErrors(err);
    }

}
