package fr.ensimag.deca;

/**
 * Exception raised when the command-line options are incorrect.
 *
 * @author gl56
 * @date 01/01/2017
 */
public class CLIException extends Exception {
    private static final long serialVersionUID = 6144682285316920966L;

    public CLIException(final String message) {
        super(message);
    }
}
