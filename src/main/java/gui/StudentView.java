package gui;

import core.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;

public class StudentView {
    private BorderPane mainLayout;

    // Inputs
    private TextField nameField;
    private Spinner<Integer> ageSpinner;
    private TextField gradeField;
    private DatePicker enrollmentDatePicker;
    private ListView<String> courseList;

    // Buttons
    private Button addButton;
    private Button removeButton;
    private Button updateButton;
    private Button displayButton;
    private Button avgButton;
    private Button searchButton;
    private Button exportButton;
    private Button importButton;

    // Search
    private TextField searchField;

    // Output
    private TableView<Student> studentTable;
    private Pagination pagination;
    private TextArea logArea;
    private TabPane tabPane;
    private BarChart<String, Number> gradeChart;

    public StudentView() {
        mainLayout = new BorderPane();
        initializeUI();
    }

    private void initializeUI() {
        // Load CSS
        try {
            mainLayout.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load styles.css: " + e.getMessage());
        }

        // --- Left Panel: Inputs ---
        VBox inputPanel = new VBox(15);
        inputPanel.getStyleClass().add("card");
        inputPanel.setPadding(new Insets(20));
        inputPanel.setPrefWidth(340);

        Label header = new Label("Student Manager");
        header.getStyleClass().add("header-label");

        // Inputs Container
        VBox fieldsBox = new VBox(12);

        nameField = new TextField();
        nameField.setPromptText("Ex: John Doe");
        VBox nameBox = new VBox(5, new Label("Name"), nameField);

        ageSpinner = new Spinner<>(18, 100, 20);
        ageSpinner.setEditable(true);
        ageSpinner.setMaxWidth(Double.MAX_VALUE);
        VBox ageBox = new VBox(5, new Label("Age"), ageSpinner);

        gradeField = new TextField();
        gradeField.setPromptText("0.0 - 100.0");
        VBox gradeBox = new VBox(5, new Label("Grade"), gradeField);

        // Name Validation
        nameField.setTextFormatter(new TextFormatter<>(change -> {
            if (change.getControlNewText().matches("[a-zA-Z\\s-]*"))
                return change;
            return null;
        }));

        // Grade Validation (Numeric)
        gradeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                gradeField.setText(oldVal);
            }
        });

        enrollmentDatePicker = new DatePicker();
        enrollmentDatePicker.setMaxWidth(Double.MAX_VALUE);
        VBox dateBox = new VBox(5, new Label("Enrollment Date"), enrollmentDatePicker);

        courseList = new ListView<>();
        courseList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        courseList.getItems().addAll("CS101", "CS102", "MATH101", "MATH201", "ENG101", "PHYS101", "CHEM101",
                "HIST101", "ART101", "MUS101", "BIO101", "ECON101", "POLS101");
        courseList.setPrefHeight(150);
        VBox courseBox = new VBox(5, new Label("Courses (Cmd/Ctrl+Click)"), courseList);

        fieldsBox.getChildren().addAll(nameBox, ageBox, gradeBox, dateBox, courseBox);

        // Buttons
        addButton = new Button("Add Student");
        addButton.setMaxWidth(Double.MAX_VALUE);
        addButton.getStyleClass().add("button-primary");

        updateButton = new Button("Update Selected");
        updateButton.setMaxWidth(Double.MAX_VALUE);
        updateButton.getStyleClass().add("button-warning");

        removeButton = new Button("Remove Selected");
        removeButton.setMaxWidth(Double.MAX_VALUE);
        removeButton.getStyleClass().add("button-danger");

        displayButton = new Button("Refresh List");
        displayButton.setMaxWidth(Double.MAX_VALUE);
        displayButton.getStyleClass().add("button-ghost");

        VBox actionButtons = new VBox(10, addButton, updateButton, removeButton, displayButton);

        inputPanel.getChildren().addAll(header, fieldsBox, new Separator(), actionButtons);

        // --- Top Panel: Toolkit ---
        HBox topPanel = new HBox(15);
        topPanel.setPadding(new Insets(15));
        topPanel.getStyleClass().add("card");
        topPanel.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        searchField = new TextField();
        searchField.setPromptText("Search Name, ID, Course...");
        searchField.setPrefWidth(350);

        searchButton = new Button("Search");
        searchButton.getStyleClass().add("button-primary");

        avgButton = new Button("Class Average");
        exportButton = new Button("Export CSV");
        importButton = new Button("Import CSV");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topPanel.getChildren().addAll(new Label("Search:"), searchField, searchButton, spacer, avgButton, exportButton,
                importButton);

        // --- Center Panel: Tabs ---
        tabPane = new TabPane();
        tabPane.getStyleClass().add("main-tabs");

        // Tab 1: Table
        Tab tableTab = new Tab("Student List");
        tableTab.setClosable(false);

        studentTable = new TableView<>();
        setupTable();

        pagination = new Pagination(1, 0);
        // We will let the controller handle the factory, but we need to initialize it
        // here or leave it empty?
        // Ideally we start with a dummy page.
        pagination.setPageFactory(pageIndex -> new VBox());

        VBox tableContainer = new VBox(10, studentTable, pagination);
        tableContainer.setPadding(new Insets(10));
        VBox.setVgrow(studentTable, Priority.ALWAYS);

        tableTab.setContent(tableContainer);

        // Tab 2: Logs
        Tab logTab = new Tab("System Logs");
        logTab.setClosable(false);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-family: 'Consolas', 'Monospaced';");
        logTab.setContent(logArea);

        // Tab 3: Charts
        Tab chartTab = new Tab("Statistics");
        chartTab.setClosable(false);

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Grade Range");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Student Count");

        gradeChart = new BarChart<>(xAxis, yAxis);
        gradeChart.setTitle("Grade Distribution");
        gradeChart.setAnimated(false);
        chartTab.setContent(gradeChart);

        tabPane.getTabs().addAll(tableTab, chartTab, logTab);

        mainLayout.setLeft(inputPanel);
        mainLayout.setTop(topPanel);
        mainLayout.setCenter(tabPane);

        // Final layout tweaks
        BorderPane.setMargin(inputPanel, new Insets(10));
        BorderPane.setMargin(topPanel, new Insets(10, 10, 0, 0));
        BorderPane.setMargin(tabPane, new Insets(10, 10, 10, 0));
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<Student, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("studentID"));

        TableColumn<Student, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Student, Integer> ageCol = new TableColumn<>("Age");
        ageCol.setCellValueFactory(new PropertyValueFactory<>("age"));

        TableColumn<Student, Double> gradeCol = new TableColumn<>("Grade");
        gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));

        TableColumn<Student, String> dateCol = new TableColumn<>("Enrolled");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));

        TableColumn<Student, String> courseCol = new TableColumn<>("Courses");
        courseCol.setCellValueFactory(cellData -> {
            var courses = cellData.getValue().getCourses();
            String res = (courses == null || courses.isEmpty()) ? "None" : String.join(", ", courses);
            return new SimpleStringProperty(res);
        });

        studentTable.getColumns().addAll(idCol, nameCol, ageCol, gradeCol, dateCol, courseCol);
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void appendLog(String message) {
        logArea.appendText(message + "\n");
    }

    public Parent getView() {
        return mainLayout;
    }

    // Getters
    public TextField getNameField() {
        return nameField;
    }

    public Spinner<Integer> getAgeSpinner() {
        return ageSpinner;
    }

    public TextField getGradeField() {
        return gradeField;
    }

    public DatePicker getEnrollmentDatePicker() {
        return enrollmentDatePicker;
    }

    public ListView<String> getCourseList() {
        return courseList;
    }

    public Button getAddButton() {
        return addButton;
    }

    public Button getRemoveButton() {
        return removeButton;
    }

    public Button getUpdateButton() {
        return updateButton;
    }

    public Button getDisplayButton() {
        return displayButton;
    }

    public Button getAvgButton() {
        return avgButton;
    }

    public Button getSearchButton() {
        return searchButton;
    }

    public Button getExportButton() {
        return exportButton;
    }

    public Button getImportButton() {
        return importButton;
    }

    public TextField getSearchField() {
        return searchField;
    }

    public TableView<Student> getStudentTable() {
        return studentTable;
    }

    public BarChart<String, Number> getGradeChart() {
        return gradeChart;
    }

    public Pagination getPagination() {
        return pagination;
    }
}
