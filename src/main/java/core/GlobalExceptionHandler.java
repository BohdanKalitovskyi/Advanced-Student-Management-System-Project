package core;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global exception handler for uncaught exceptions in the application.
 * 
 * <p>
 * This class implements the {@link UncaughtExceptionHandler} interface to
 * provide
 * centralized error handling for exceptions that are not caught elsewhere in
 * the application.
 * All uncaught exceptions are logged using SLF4J for debugging and monitoring
 * purposes.
 * </p>
 * 
 * <p>
 * To use this handler, set it as the default uncaught exception handler:
 * </p>
 * 
 * <pre>
 * Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
 * </pre>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class GlobalExceptionHandler implements UncaughtExceptionHandler {
    /**
     * Logger instance for recording uncaught exceptions.
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles an uncaught exception by logging it with full stack trace.
     * 
     * <p>
     * This method is automatically called by the JVM when a thread terminates
     * due to an uncaught exception. The exception details are logged at ERROR level
     * including the thread name and complete stack trace.
     * </p>
     * 
     * @param t the thread that terminated due to the exception
     * @param e the uncaught exception that caused the thread to terminate
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        logger.error("Uncaught exception in thread {}", t.getName(), e);
    }
}
