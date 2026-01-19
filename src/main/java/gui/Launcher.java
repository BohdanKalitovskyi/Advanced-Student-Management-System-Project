package gui;

/**
 * Entry point launcher for the JavaFX Student Management Application.
 * 
 * <p>
 * This class serves as the main entry point for the GUI application. It
 * delegates
 * to {@link StudentManagementApp} to launch the JavaFX application. This
 * separation
 * allows for proper JavaFX module initialization and avoids potential class
 * loading issues.
 * </p>
 * 
 * <p>
 * To run the application, execute this class's main method or use Maven:
 * </p>
 * 
 * <pre>
 * mvn javafx:run
 * </pre>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 * @see StudentManagementApp
 */
public class Launcher {
    /**
     * Main entry point for the application.
     * 
     * <p>
     * Launches the JavaFX application by delegating to StudentManagementApp.
     * </p>
     * 
     * @param args command-line arguments (passed to JavaFX application)
     */
    public static void main(String[] args) {
        StudentManagementApp.main(args);
    }
}
