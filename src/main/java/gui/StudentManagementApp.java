package gui;

import core.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

/**
 * Main JavaFX Application class for the Student Management System.
 * 
 * <p>
 * This class extends {@link javafx.application.Application} and serves as the
 * entry point
 * for the JavaFX GUI. It initializes the MVC (Model-View-Controller)
 * architecture by creating
 * the view and controller components, and sets up the primary stage with the
 * application scene.
 * </p>
 * 
 * <p>
 * The application uses a BorderPane as the root layout and displays the student
 * management
 * interface in a 1280x800 window.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class StudentManagementApp extends Application {

    /**
     * Initializes and displays the JavaFX application window.
     * 
     * <p>
     * This method is called by the JavaFX runtime after the application is
     * launched.
     * It performs the following initialization:
     * </p>
     * <ol>
     * <li>Creates a BorderPane as the root layout</li>
     * <li>Initializes the StudentView (MVC View component)</li>
     * <li>Initializes the StudentController (MVC Controller component)</li>
     * <li>Sets up the scene with 1280x800 dimensions</li>
     * <li>Configures and displays the primary stage</li>
     * </ol>
     * 
     * @param primaryStage the primary stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler());
        try {
            BorderPane root = new BorderPane();

            // Initialize MVC
            StudentView view = new StudentView();
            StudentController controller = new StudentController(view);

            root.setCenter(view.getView());

            Scene scene = new Scene(root, 1280, 800);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Student Management System");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
