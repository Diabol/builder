package utils;

public class DataInconsistencyException extends RuntimeException {

    public DataInconsistencyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataInconsistencyException(String message) {
        super(message);
    }
}
