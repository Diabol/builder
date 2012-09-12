package models.config;

/**
 * Thrown if there is a problem with the Pipe config, e.g. no phases.
 * 
 * @author marcus
 */
public class PipeValidationException extends Exception {

    public PipeValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public PipeValidationException(String message) {
        super(message);
    }

}
