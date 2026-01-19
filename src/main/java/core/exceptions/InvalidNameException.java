package core.exceptions;

/**
 * Exception thrown when a student's name fails validation.
 * 
 * <p>
 * This exception is thrown when attempting to set a student's name to a value
 * that doesn't match the required pattern. Valid names must contain only
 * letters
 * (a-z, A-Z), spaces, and hyphens. Any other characters including numbers or
 * special symbols will cause this exception to be thrown.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 * @see core.Student#setName(String)
 */
public class InvalidNameException extends RuntimeException {
    /**
     * Constructs a new InvalidNameException with the specified detail message.
     * 
     * @param message the detail message explaining why the name is invalid
     */
    public InvalidNameException(String message) {
        super(message);
    }
}