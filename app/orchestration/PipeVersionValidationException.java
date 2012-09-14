package orchestration;

/**
 * Thrown if we can't create a new {@link PipeVersion} due to som inconsistencies in input.
 * 
 * Considered a runtime (non-checked) exception since it's a configuration or programming error.
 * 
 * @author marcus
 */
public class PipeVersionValidationException extends RuntimeException {

    public PipeVersionValidationException(String message) {
        super(message);
    }

    public PipeVersionValidationException(String message, Throwable cause) {
        super(message, cause);
    }

}
