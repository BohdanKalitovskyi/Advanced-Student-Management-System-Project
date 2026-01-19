package core.exceptions;

/**
 * Exception thrown when a student's age fails validation.
 * 
 * <p>
 * This exception is thrown when attempting to set a student's age to a value
 * outside the valid range of 18-100 years (inclusive). It extends
 * RuntimeException
 * to allow for unchecked exception handling.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 * @see core.Student#setAge(int)
 */
public class InvalidAgeException extends RuntimeException {
    /**
     * Constructs a new InvalidAgeException with the specified detail message.
     * 
     * @param message the detail message explaining why the age is invalid
     */
    public InvalidAgeException(String message) {
        super(message);
    }
}