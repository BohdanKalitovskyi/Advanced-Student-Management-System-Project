package gui;

import core.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller component of the MVC pattern for the Student Management GUI.
 * 
 * <p>
 * This class handles all user interactions and coordinates between the
 * StudentView
 * (presentation layer) and StudentManagerImpl (business logic layer). It
 * manages:
 * </p>
 * <ul>
 * <li>Event handling for all UI buttons and controls</li>
 * <li>Background task execution for database operations</li>
 * <li>Pagination of student data (15 rows per page)</li>
 * <li>Real-time search functionality</li>
 * <li>Data validation and error handling</li>
 * <li>Chart updates for grade distribution</li>
 * </ul>
 * 
 * <p>
 * All database operations are executed in background threads to prevent UI
 * freezing.
 * The controller uses JavaFX Task API for asynchronous operations with proper
 * success
 * and failure handlers.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class StudentController {
    /**
     * Number of student rows displayed per page in the table.
     */
    private static final int ROWS_PER_PAGE = 15;

    private StudentView view;
    private StudentManagerImpl manager;
    private ObservableList<Student> studentList;
    private ArrayList<Student> fullDataList = new ArrayList<>();

    /**
     * Constructs a new StudentController and initializes the system components.
     * Sets up the manager singleton and prepares the student data list.
     *
     * @param view The view component of the MVC architecture.
     */
    public StudentController(StudentView view) {
        this.view = view;
        this.manager = StudentManagerImpl.getInstance();
        this.studentList = FXCollections.observableArrayList();

        initController();
    }

    /**
     * Initializes the controller by binding events and setting up the UI state.
     * Connects all buttons to their respective actions and initializes pagination.
     */
    private void initController() {
        // Link Table to ObservableList
        view.getStudentTable().setItems(studentList);

        // Setup Pagination
        view.getPagination().setPageFactory(this::createPage);

        // Load initial data
        refreshTable();

        // Event Listeners
        view.getAddButton().setOnAction(e -> addStudent());
        view.getRemoveButton().setOnAction(e -> removeStudent());
        view.getUpdateButton().setOnAction(e -> updateStudent());
        view.getDisplayButton().setOnAction(e -> refreshTable());
        view.getSearchButton().setOnAction(e -> searchStudent());
        view.getAvgButton().setOnAction(e -> calculateAverage());
        view.getExportButton().setOnAction(e -> exportCSV());
        view.getImportButton().setOnAction(e -> importCSV());

        // Live search
        view.getSearchField().textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                refreshTable();
            } else {
                searchStudent();
            }
        });
    }

    /**
     * Creates a page of student data for the pagination control.
     *
     * @param pageIndex The index of the page to create.
     * @return A {@link Node} representing the table for the specified page.
     */
    private Node createPage(int pageIndex) {
        int fromIndex = pageIndex * ROWS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, fullDataList.size());

        if (fromIndex < toIndex) {
            studentList.setAll(fullDataList.subList(fromIndex, toIndex));
        } else {
            studentList.clear();
        }

        return new VBox(); // Dummy node, as we update items directly
    }

    /**
     * Updates the pagination control based on the current student list size.
     * Recalculates the total number of pages and resets the UI.
     */
    private void updatePagination() {
        int pageCount = (int) Math.ceil((double) fullDataList.size() / ROWS_PER_PAGE);
        if (pageCount == 0)
            pageCount = 1;
        view.getPagination().setPageCount(pageCount);

        // Reset to first page or keep current if valid
        if (view.getPagination().getCurrentPageIndex() >= pageCount) {
            view.getPagination().setCurrentPageIndex(0);
        } else {
            // Force refresh of current page
            createPage(view.getPagination().getCurrentPageIndex());
        }
        updateCharts();
    }

    /**
     * Refreshes the student table by fetching the latest data from the database.
     * This operation is performed asynchronously to keep the UI responsive.
     */
    private void refreshTable() {
        Task<ArrayList<Student>> task = new Task<>() {
            @Override
            protected ArrayList<Student> call() throws Exception {
                return manager.displayAllStudents();
            }
        };

        task.setOnSucceeded(e -> {
            fullDataList = task.getValue();
            updatePagination();
            view.appendLog("Refreshed list. Total students: " + fullDataList.size());
        });

        task.setOnFailed(e -> {
            view.appendLog("Error refreshing table: " + task.getException().getMessage());
        });

        new Thread(task).start();
    }

    /**
     * Handles the addition of a new student to the system.
     * Validates input fields, creates a {@link Student} object, and persists it.
     */
    private void addStudent() {
        try {
            String name = view.getNameField().getText();
            int age = view.getAgeSpinner().getValue();
            String gradeText = view.getGradeField().getText();
            LocalDate date = view.getEnrollmentDatePicker().getValue();

            if (name.isEmpty() || date == null || gradeText.isEmpty()) {
                showAlert("Validation Error", "Name, Grade, and Date are required.");
                return;
            }

            double grade = 0.0;
            try {
                grade = Double.parseDouble(gradeText);
            } catch (NumberFormatException e) {
                showAlert("Validation Error", "Grade must be a valid number.");
                return;
            }

            // Validate grade range
            if (grade < 0 || grade > 100) {
                showAlert("Validation Error", "Grade must be between 0 and 100.");
                return;
            }

            ArrayList<String> selectedCourses = new ArrayList<>(
                    view.getCourseList().getSelectionModel().getSelectedItems());

            Student s = new Student(name, age, grade, date, selectedCourses);

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    manager.addStudent(s);
                    // Add courses
                    for (String courseCode : selectedCourses) {
                        manager.addCourseToStudent(s.getStudentID(), courseCode, "Course " + courseCode, 4);
                    }
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                view.appendLog("Added student: " + name);
                refreshTable();
                clearInputs();
            });

            task.setOnFailed(e -> {
                view.appendLog("Error adding student: " + task.getException().getMessage());
                showAlert("Error", task.getException().getMessage());
            });

            new Thread(task).start();

        } catch (Exception e) {
            view.appendLog("Error adding student: " + e.getMessage());
            showAlert("Error", e.getMessage());
        }
    }

    /**
     * Removes the currently selected student from the database.
     * Prompts the user with a confirmation dialog before deletion.
     */
    private void removeStudent() {
        var selectedItems = new ArrayList<>(view.getStudentTable().getSelectionModel().getSelectedItems());
        if (selectedItems.isEmpty()) {
            showAlert("Selection Error", "Please select student(s) to remove.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Confirmation");
        alert.setHeaderText("Remove Student(s)");
        alert.setContentText("Are you sure you want to remove " + selectedItems.size() + " student(s)?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    for (Student s : selectedItems) {
                        manager.removeStudent(s.getStudentID());
                    }
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                view.appendLog("Removed " + selectedItems.size() + " student(s).");
                refreshTable();
            });

            new Thread(task).start();
        }
    }

    /**
     * Updates an existing student's information in the database.
     * Values are taken from the current input fields and applied to the selected
     * student.
     */
    private void updateStudent() {
        Student selected = view.getStudentTable().getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Selection Error", "Please select a student to update.");
            return;
        }

        javafx.scene.control.Dialog<Student> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Update Student");
        dialog.setHeaderText("Update details for " + selected.getName());

        ButtonType updateButtonType = new ButtonType("Update", javafx.scene.control.ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception ignored) {
        }

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        javafx.scene.control.TextField name = new javafx.scene.control.TextField(selected.getName());
        javafx.scene.control.Spinner<Integer> age = new javafx.scene.control.Spinner<>(18, 100, selected.getAge());
        age.setEditable(true);

        javafx.scene.control.TextField grade = new javafx.scene.control.TextField(String.valueOf(selected.getGrade()));

        javafx.scene.control.DatePicker date = new javafx.scene.control.DatePicker(selected.getEnrollmentDate());

        grid.add(new javafx.scene.control.Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new javafx.scene.control.Label("Age:"), 0, 1);
        grid.add(age, 1, 1);
        grid.add(new javafx.scene.control.Label("Grade:"), 0, 2);
        grid.add(grade, 1, 2);
        grid.add(new javafx.scene.control.Label("Enrollment Date:"), 0, 3);
        grid.add(date, 1, 3);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == updateButtonType) {
                try {
                    double g = Double.parseDouble(grade.getText());
                    return new Student(selected.getStudentID(), name.getText(), age.getValue(), g,
                            date.getValue(), selected.getCourses());
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        });

        Optional<Student> result = dialog.showAndWait();

        result.ifPresent(updatedStudent -> {
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    manager.updateStudent(selected.getStudentID(), updatedStudent);
                    return null;
                }
            };

            task.setOnSucceeded(e -> {
                view.appendLog("Updated student: " + updatedStudent.getName());
                refreshTable();
            });

            new Thread(task).start();
        });
    }

    /**
     * Updates the UI charts based on the current student data.
     * Categorizes students into grade buckets for visualization.
     */
    private void updateCharts() {
        int[] ranges = new int[5]; // 0-59, 60-69, 70-79, 80-89, 90-100
        for (Student s : fullDataList) {
            double g = s.getGrade();
            if (g < 60)
                ranges[0]++;
            else if (g < 70)
                ranges[1]++;
            else if (g < 80)
                ranges[2]++;
            else if (g < 90)
                ranges[3]++;
            else
                ranges[4]++;
        }

        view.getGradeChart().getData().clear();
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Grade Distribution");
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("0-59", ranges[0]));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("60-69", ranges[1]));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("70-79", ranges[2]));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("80-89", ranges[3]));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("90-100", ranges[4]));
        view.getGradeChart().getData().add(series);
    }

    /**
     * Performs a real-time search on the student list.
     * Filters the table automatically as the user types.
     */
    private void searchStudent() {
        String query = view.getSearchField().getText().toLowerCase();
        Task<ArrayList<Student>> task = new Task<>() {
            @Override
            protected ArrayList<Student> call() {
                return manager.searchStudents(query);
            }
        };

        task.setOnSucceeded(e -> {
            fullDataList = task.getValue();
            updatePagination();
            view.appendLog("Search completed for: " + query);
        });

        new Thread(task).start();
    }

    /**
     * Calculates the average grade of all students and displays it in an alert.
     */
    private void calculateAverage() {
        Task<Double> task = new Task<>() {
            @Override
            protected Double call() {
                return manager.calculateAverageGrade();
            }
        };

        task.setOnSucceeded(e -> {
            double avg = task.getValue();
            showAlert("Average Grade", String.format("The average grade of all students is: %.2f", avg));
            view.appendLog("Calculated average grade: " + avg);
        });

        new Thread(task).start();
    }

    /**
     * Exports the current student list to a CSV file selected by the user.
     */
    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            manager.exportStudentsToCSV(file.getAbsolutePath());
            view.appendLog("Exported student data to: " + file.getName());
        }
    }

    /**
     * Imports student data from a CSV file selected by the user.
     */
    private void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import from CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            manager.importStudentsFromCSV(file.getAbsolutePath());
            refreshTable();
            view.appendLog("Imported student data from: " + file.getName());
        }
    }

    /**
     * Clears all input fields in the student entry form.
     */
    private void clearInputs() {
        view.getNameField().clear();
        view.getAgeSpinner().getValueFactory().setValue(20);
        view.getGradeField().clear();
        view.getEnrollmentDatePicker().setValue(LocalDate.now());
        view.getCourseList().getSelectionModel().clearSelection();
    }

    /**
     * Helper method to show a popup alert message to the user.
     *
     * @param title   The title of the alert window.
     * @param message The content message of the alert.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
