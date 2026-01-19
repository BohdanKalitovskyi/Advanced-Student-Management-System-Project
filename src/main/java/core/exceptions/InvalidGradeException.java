package core.exceptions;

/**
 * Exception thrown when a student's grade fails validation.
 * 
 * <p>
 * This exception is thrown when attempting to set a student's grade to a value
 * outside the valid range of 0.0-100.0 (inclusive). Grades represent
 * percentages
 * and must fall within this range to be considered valid.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 * @see core.Student#setGrade(double)
 */
public class InvalidGradeException extends RuntimeException {
    /**
     * Constructs a new InvalidGradeException with the specified detail message.
     * 
     * @param message the detail message explaining why the grade is invalid
     */
    public InvalidGradeException(String message) {
        super(message);
    }
}