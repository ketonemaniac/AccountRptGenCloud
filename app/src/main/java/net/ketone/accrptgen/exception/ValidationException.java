package net.ketone.accrptgen.exception;

/**
 * Exception when input files do not match expected format
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
