package gui;

import core.*;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;

public class StudentManagementApp extends Application {

    @Override
    public void start(Stage primaryStage) {
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
