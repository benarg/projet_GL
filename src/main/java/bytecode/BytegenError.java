package bytecode;

import fr.ensimag.deca.tree.Location;
import fr.ensimag.deca.tree.LocationException;

/**
 * Exception raised when a bytegen error is found.
 *
 * @author gl56
 * @date 22/01/2017
 */
public class BytegenError extends LocationException {
    private static final long serialVersionUID = -2984027495734665920L;

    public BytegenError(String message, Location location) {
        super(message, location);
    }
}
