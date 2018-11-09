package fr.ensimag.deca;

import java.io.File;
import java.io.PrintStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * User-specified options influencing the compilation.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class CompilerOptions {
    public static final int QUIET = 0;
    public static final int INFO  = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    public int getDebug() {
        return debug;
    }

    public boolean getParallel() {
        return parallel;
    }

    public boolean getPrintBanner() {
        return printBanner;
    }

    public boolean getParse() {
        return parse;
    }
    public boolean getVerification() {
        return verification;
    }
    public boolean getNoCheck() {
        return noCheck;
    }
    public int getNbRegister() {
        return nbRegister;
    }

    public boolean getByteCode() {
        return byteCode;
    }

    
    public List<File> getSourceFiles() {
        return Collections.unmodifiableList(sourceFiles);
    }

    private void addSourceFile(String fileName) throws CLIException{
        if ((fileName.indexOf('.') == -1) 
            || !fileName.substring(fileName.lastIndexOf('.'), fileName.length()).equals(".deca"))
            throw new CLIException("le fichier " + fileName + " n'a pas une extension .deca");
        File sourceFile = new File(fileName);
        if (!sourceFile.exists()) 
            throw new CLIException("le fichier " + fileName + " inconnu ou illisible");
        sourceFiles.add(sourceFile);
    }

    private int debug = 0;
    private boolean parallel = false;
    private boolean printBanner = false;
    private boolean parse = false;
    private boolean verification = false;
    private boolean noCheck = false;
    private int nbRegister = 16;
    private boolean byteCode = false;
    private List<File> sourceFiles = new ArrayList<File>();

    
    public void parseArgs(String[] args) throws CLIException {
        boolean onlySrcLeft = false;
        int i = 0;
        while (i < args.length) {
            if (printBanner)
                throw new CLIException("not allowed another argument with option -b");
            if (onlySrcLeft) 
                addSourceFile(args[i]);
            else {
                if (args[i].charAt(0) != '-') {
                    addSourceFile(args[i]);
                    onlySrcLeft = true;
                }
                else {
                    if (args[i].length() != 2) {
                        throw new CLIException("option " + args[i] + " unknown");
                    }
                    switch (args[i].charAt(1)) {
                        case 'b':
                            if (i != 0)
                                throw new CLIException("not allowed another argument with option " + args[i]);
                            printBanner = true;
                            break;
                        case 'P':
                            parallel = true;
                            //throw new CLIException("option " + args[i] + " not yet implemented");
                            break;
                        case 'p':
                            if (verification)
                                throw new CLIException("not allowed to use option -p and -v together");
                            parse = true;
                            //throw new CLIException("option " + args[i] + " not yet implemented");
                            break;
                        case 'v':
                            if (parse)
                                throw new CLIException("not allowed to use option -p and -v together");
                            verification = true;
                            //throw new CLIException("option " + args[i] + " not yet implemented");
                            break;
                        case 'n':
                            noCheck = true;
                            break;
                        case 'r':
                            i++;
                            if (i == args.length)
                                throw new CLIException("missing number of registers for option " + args[i]);
                            try {
                                nbRegister = Integer.parseInt(args[i]);
                            } catch (NumberFormatException e) {
                                throw new CLIException("expected integer for option -r, got " + args[i]);
                            }
                            if ((nbRegister < 4) || (nbRegister > 16))
                                throw new CLIException("expected integer between 4 and 16, got " + args[i]);
                            break;
                        case 'B':
                            byteCode = true;
                            break;
                        case 'd':
                            debug++;
                            break;
                        default:
                            throw new CLIException("option " + args[i] + " unknown");
                            //break;
                    }
                }
            }
            i++;
        }
        
        // A FAIRE : parcourir args pour positionner les options correctement.
        Logger logger = Logger.getRootLogger();
        // map command-line debug option to log4j's level.
        switch (getDebug()) {
        case QUIET: break; // keep default
        case INFO:
            logger.setLevel(Level.INFO); break;
        case DEBUG:
            logger.setLevel(Level.DEBUG); break;
        case TRACE:
            logger.setLevel(Level.TRACE); break;
        default:
            logger.setLevel(Level.ALL); break;
        }
        logger.info("Application-wide trace level set to " + logger.getLevel());

        boolean assertsEnabled = false;
        assert assertsEnabled = true; // Intentional side effect!!!
        if (assertsEnabled) {
            logger.info("Java assertions enabled");
        } else {
            logger.info("Java assertions disabled");
        }
    }

    public void getBanner(String bannerDirectory){
        BufferedReader buffer = null;
        InputStreamReader reader = null;
        InputStream bannerFile;
        final URL bannerURL = ClassLoader.getSystemResource("include/"+bannerDirectory);
        if(bannerURL == null){
            System.out.println("Banner not found");
            System.exit(1);
        }
        try {
            bannerFile = bannerURL.openStream();
            reader = new InputStreamReader(bannerFile);
            buffer = new BufferedReader(reader);
            String sCurrentLine;
            while ((sCurrentLine = buffer.readLine()) != null) {
                System.out.println(sCurrentLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void displayUsage() {
        String usage = "decac [[-p | -v] [-n] [-r X] <fichier deca>...] | [-b]";
        System.out.println("Usage: \n" + usage);
    }
}
