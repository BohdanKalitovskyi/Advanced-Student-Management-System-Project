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

    public StudentController(StudentView view) {
        this.view = view;
        this.manager = StudentManagerImpl.getInstance();
        this.studentList = FXCollections.observableArrayList();

        initController();
    }

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

    private void updateCharts() {
        javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
        series.setName("Grades");

        int a = 0, b = 0, c = 0, d = 0, f = 0;

        // Use full list for stats, not just paged view
        for (Student s : fullDataList) {
            double g = s.getGrade();
            if (g >= 90)
                a++;
            else if (g >= 80)
                b++;
            else if (g >= 70)
                c++;
            else if (g >= 60)
                d++;
            else
                f++;
        }

        series.getData().add(new javafx.scene.chart.XYChart.Data<>("A (90-100)", a));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("B (80-89)", b));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("C (70-79)", c));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("D (60-69)", d));
        series.getData().add(new javafx.scene.chart.XYChart.Data<>("F (<60)", f));

        view.getGradeChart().getData().clear();
        view.getGradeChart().getData().add(series);
    }

    private void searchStudent() {
        String query = view.getSearchField().getText();
        Task<ArrayList<Student>> task = new Task<>() {
            @Override
            protected ArrayList<Student> call() throws Exception {
                return manager.searchStudents(query);
            }
        };

        task.setOnSucceeded(e -> {
            fullDataList = task.getValue();
            updatePagination();
            view.appendLog("Search results for: " + query + " (" + fullDataList.size() + ")");
        });

        new Thread(task).start();
    }

    private void calculateAverage() {
        Task<Double> task = new Task<>() {
            @Override
            protected Double call() throws Exception {
                // Calculate average of currently displayed (or filtered) students?
                // Or global average? Using Logic from Manager which is global.
                // If we want filtered average:
                return fullDataList.stream()
                        .mapToDouble(Student::getGrade)
                        .average().orElse(0.0);
            }
        };

        task.setOnSucceeded(e -> {
            showAlert("Average Grade", String.format("Average (Current View): %.2f", task.getValue()));
        });

        new Thread(task).start();
    }

    private void exportCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            manager.exportStudentsToCSV(file.getAbsolutePath());
            view.appendLog("Exported to " + file.getAbsolutePath());
        }
    }

    private void importCSV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            manager.importStudentsFromCSV(file.getAbsolutePath());
            view.appendLog("Imported from " + file.getAbsolutePath());
            refreshTable();
        }
    }

    private void clearInputs() {
        view.getNameField().clear();
        view.getAgeSpinner().getValueFactory().setValue(20);
        view.getGradeField().clear();
        view.getEnrollmentDatePicker().setValue(null);
        view.getCourseList().getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
