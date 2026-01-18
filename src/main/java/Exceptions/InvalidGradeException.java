package Exceptions;

public class InvalidGradeException extends RuntimeException {
    public InvalidGradeException(String message) { super(message); }
}