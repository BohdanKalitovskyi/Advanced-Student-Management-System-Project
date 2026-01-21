package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Singleton implementation of the StudentManager interface using SQLite
 * database.
 * 
 * <p>
 * This class provides a complete implementation of student management
 * operations with
 * database persistence using JDBC and SQLite. It implements the Singleton
 * pattern to ensure
 * only one instance manages the database connection throughout the application
 * lifecycle.
 * </p>
 * 
 * <p>
 * Key features:
 * </p>
 * <ul>
 * <li>CRUD operations for students with database persistence</li>
 * <li>Transaction support for data integrity</li>
 * <li>SLF4J logging for debugging and monitoring</li>
 * <li>CSV import/export capabilities</li>
 * <li>Advanced search across multiple fields</li>
 * <li>Course enrollment management</li>
 * </ul>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class StudentManagerImpl implements StudentManager {
    /**
     * Logger instance for recording operations and errors.
     */
    private static final Logger logger = LoggerFactory.getLogger(StudentManagerImpl.class);

    /**
     * Singleton instance of StudentManagerImpl.
     */
    private static StudentManagerImpl instance;

    /**
     * Obtains a database connection from the ConnectionFactory.
     * 
     * <p>
     * This method is protected to allow test subclasses to override
     * the connection source for testing purposes.
     * </p>
     * 
     * @return a new database connection
     * @throws SQLException if a database access error occurs
     */
    protected Connection getConnection() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    /**
     * Protected constructor for singleton pattern.
     * Initializes the database schema on first instantiation.
     */
    protected StudentManagerImpl() {
        initializeDatabase();
    }

    /**
     * Initializes the database schema.
     * 
     * <p>
     * This method is protected to allow test subclasses to override
     * the initialization behavior.
     * </p>
     */
    protected void initializeDatabase() {
        DatabaseInitializer.initialize();
    }

    /**
     * Returns the singleton instance of StudentManagerImpl.
     * 
     * <p>
     * This method is thread-safe and creates the instance on first access
     * (lazy initialization). The synchronized keyword ensures only one instance
     * is created even in multi-threaded environments.
     * </p>
     * 
     * @return the singleton StudentManagerImpl instance
     */
    public static synchronized StudentManagerImpl getInstance() {
        if (instance == null) {
            instance = new StudentManagerImpl();
        }
        return instance;
    }

    /**
     * Adds a new student to the database.
     * 
     * <p>
     * This method performs the following operations within a transaction:
     * </p>
     * <ol>
     * <li>Checks if a student with the same ID already exists</li>
     * <li>Inserts the student record into the students table</li>
     * <li>Creates course records if they don't exist</li>
     * <li>Creates enrollment records linking the student to their courses</li>
     * </ol>
     * 
     * <p>
     * If a student with the same ID already exists, the operation is skipped
     * and a warning is logged. All operations are performed within a transaction
     * that is rolled back if any error occurs.
     * </p>
     * 
     * @param student the student to add (must not be null)
     */
    @Override
    public void addStudent(Student student) {
        String checkSql = "SELECT 1 FROM students WHERE studentID = ?";
        String insertSql = """
                    INSERT INTO students (studentID, name, age, grade, enrollmentDate)
                    VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setString(1, student.getStudentID());
                try (ResultSet rs = checkPs.executeQuery()) {
                    if (rs.next()) {
                        logger.warn("Attempted to add existing student with ID: {}", student.getStudentID());
                        conn.rollback();
                        return;
                    }
                }

                try (PreparedStatement insertPs = conn.prepareStatement(insertSql)) {
                    insertPs.setString(1, student.getStudentID());
                    insertPs.setString(2, student.getName());
                    insertPs.setInt(3, student.getAge());
                    insertPs.setDouble(4, student.getGrade());
                    insertPs.setString(5, student.getEnrollmentDate().toString());
                    insertPs.executeUpdate();
                }

                // Insert courses/enrollments
                if (student.getCourses() != null && !student.getCourses().isEmpty()) {
                    String courseSql = "INSERT OR IGNORE INTO courses(courseCode, courseName, credits) VALUES (?, ?, ?)";
                    String enrollSql = "INSERT OR IGNORE INTO enrollments(studentID, courseCode, enrollmentGrade) VALUES (?, ?, ?)";

                    try (PreparedStatement coursePs = conn.prepareStatement(courseSql);
                            PreparedStatement enrollPs = conn.prepareStatement(enrollSql)) {

                        for (String course : student.getCourses()) {

                            coursePs.setString(1, course);
                            coursePs.setString(2, "Course " + course);
                            coursePs.setInt(3, 4);
                            coursePs.executeUpdate();

                            enrollPs.setString(1, student.getStudentID());
                            enrollPs.setString(2, course);
                            enrollPs.setDouble(3, 0.0);
                            enrollPs.executeUpdate();
                        }
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Database error while adding student (Transaction rolled back)", e);
            }

        } catch (SQLException e) {
            logger.error("Database connection error", e);
        }
    }

    /**
     * Removes a student from the database by their ID.
     * 
     * <p>
     * This operation also removes all associated enrollment records due to
     * the ON DELETE CASCADE constraint in the database schema.
     * </p>
     * 
     * @param studentID the unique identifier of the student to remove
     */
    @Override
    public void removeStudent(String studentID) {
        String sql = "DELETE FROM students WHERE studentID = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Updates an existing student's information in the database.
     * 
     * <p>
     * Updates the student's name, age, grade, and enrollment date.
     * The student ID cannot be changed. Course enrollments are not updated
     * by this method - use course management methods separately.
     * </p>
     * 
     * @param studentID      the unique identifier of the student to update
     * @param updatedStudent the student object containing new information
     */
    @Override
    public void updateStudent(String studentID, Student updatedStudent) {
        String sql = """
                    UPDATE students
                    SET name = ?, age = ?, grade = ?, enrollmentDate = ?
                    WHERE studentID = ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, updatedStudent.getName());
            ps.setInt(2, updatedStudent.getAge());
            ps.setDouble(3, updatedStudent.getGrade());
            ps.setString(4, updatedStudent.getEnrollmentDate().toString());
            ps.setString(5, studentID);

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Retrieves all students from the database.
     * 
     * <p>
     * For each student, this method also loads their enrolled courses from
     * the enrollments table. The returned list is sorted alphabetically by
     * student name by default.
     * </p>
     * 
     * @return an ArrayList of all students with their course enrollments
     */
    @Override
    public ArrayList<Student> displayAllStudents() {
        return displayAllStudents("name");
    }

    /**
     * Retrieves all students from the database with custom sorting.
     * 
     * @param sortBy the field to sort by (e.g., "name", "grade", "age")
     * @return an ArrayList of all students with their course enrollments
     */
    @Override
    public ArrayList<Student> displayAllStudents(String sortBy) {
        ArrayList<Student> students = new ArrayList<>();

        // Map internal sort names to SQL columns
        String orderColumn = switch (sortBy.toLowerCase()) {
            case "grade" -> "grade DESC";
            case "age" -> "age";
            default -> "name";
        };

        String sql = "SELECT * FROM students ORDER BY " + orderColumn;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getString("studentID"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("grade"),
                        LocalDate.parse(rs.getString("enrollmentDate")),
                        new ArrayList<>());

                try (PreparedStatement psCourses = conn.prepareStatement(
                        "SELECT c.courseCode FROM courses c " +
                                "JOIN enrollments e ON c.courseCode = e.courseCode " +
                                "WHERE e.studentID = ?")) {
                    psCourses.setString(1, s.getStudentID());
                    ResultSet rsCourses = psCourses.executeQuery();
                    while (rsCourses.next()) {
                        s.addCourse(rsCourses.getString("courseCode"));
                    }
                }

                students.add(s);
            }

        } catch (SQLException e) {
            logger.error("Database error while retrieving students", e);
        }

        return students;
    }

    /**
     * Calculates the average grade across all students.
     * 
     * <p>
     * Only grades within the valid range (0-100) are included in the calculation.
     * If no students exist or all grades are invalid, returns 0.0.
     * </p>
     * 
     * @return the average grade as a percentage (0.0-100.0)
     */
    @Override
    public double calculateAverageGrade() {
        return displayAllStudents().stream()
                .mapToDouble(Student::getGrade)
                .filter(g -> g >= 0 && g <= 100)
                .average()
                .orElse(0.0);
    }

    /**
     * Calculates the average grade for students enrolled in a specific course.
     * 
     * @param courseCode the course to filter by
     * @return the average grade of the filtered group
     */
    @Override
    public double calculateAverageGrade(String courseCode) {
        String sql = """
                    SELECT AVG(s.grade) as avg_grade
                    FROM students s
                    JOIN enrollments e ON s.studentID = e.studentID
                    WHERE e.courseCode = ? AND s.grade >= 0 AND s.grade <= 100
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, courseCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_grade");
                }
            }
        } catch (SQLException e) {
            logger.error("Database error while calculating average for course: {}", courseCode, e);
        }
        return 0.0;
    }

    /**
     * Searches for students matching the given query string.
     * 
     * <p>
     * The search is performed across multiple fields using SQL LIKE with wildcards:
     * </p>
     * <ul>
     * <li>Student name</li>
     * <li>Student ID</li>
     * <li>Course codes</li>
     * <li>Course names</li>
     * <li>Age (converted to text)</li>
     * <li>Grade (converted to text)</li>
     * </ul>
     * 
     * <p>
     * The search is case-insensitive and supports partial matches.
     * Results include the full student object with all enrolled courses.
     * </p>
     * 
     * @param query the search term to match
     * @return an ArrayList of students matching the search criteria
     */
    @Override
    public ArrayList<Student> searchStudents(String query) {
        ArrayList<Student> result = new ArrayList<>();
        String sql = """
                    SELECT DISTINCT s.*
                    FROM students s
                    LEFT JOIN enrollments e ON s.studentID = e.studentID
                    LEFT JOIN courses c ON e.courseCode = c.courseCode
                    WHERE s.name LIKE ?
                       OR s.studentID LIKE ?
                       OR c.courseCode LIKE ?
                       OR c.courseName LIKE ?
                       OR CAST(s.age AS TEXT) LIKE ?
                       OR CAST(s.grade AS TEXT) LIKE ?
                """;

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, pattern);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String studentID = rs.getString("studentID");
                Student s = new Student(
                        studentID,
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("grade"),
                        LocalDate.parse(rs.getString("enrollmentDate")),
                        new ArrayList<>());

                try (PreparedStatement psCourses = conn.prepareStatement(
                        "SELECT courseCode FROM enrollments WHERE studentID = ?")) {
                    psCourses.setString(1, studentID);
                    ResultSet rsCourses = psCourses.executeQuery();
                    while (rsCourses.next()) {
                        s.addCourse(rsCourses.getString("courseCode"));
                    }
                }

                result.add(s);
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    /**
     * Exports all students to a CSV file.
     * 
     * <p>
     * The CSV format includes a header row and the following columns:
     * </p>
     * <ul>
     * <li>name</li>
     * <li>age</li>
     * <li>grade (formatted to 2 decimal places)</li>
     * <li>enrollmentDate</li>
     * <li>courses (semicolon-separated list)</li>
     * </ul>
     * 
     * @param filePath the path where the CSV file should be created
     */
    @Override
    public void exportStudentsToCSV(String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {

            writer.println("name,age,grade,enrollmentDate,courses");

            for (Student s : displayAllStudents()) {
                writer.printf(
                        "%s,%d,%.2f,%s,",
                        s.getName(),
                        s.getAge(),
                        s.getGrade(),
                        s.getEnrollmentDate());

                String courses = String.join(";", s.getCourses());
                writer.print(courses);
                writer.println();
            }

        } catch (Exception e) {
            logger.error("Error exporting students to CSV", e);
        }
    }

    /**
     * Imports students from a CSV file.
     * 
     * <p>
     * The CSV file must have a header row and follow this format:
     * </p>
     * <ul>
     * <li>name</li>
     * <li>age</li>
     * <li>grade</li>
     * <li>enrollmentDate (ISO format: YYYY-MM-DD)</li>
     * <li>courses (semicolon-separated list)</li>
     * </ul>
     * 
     * <p>
     * Invalid lines are skipped with a warning logged. Successfully imported
     * student count is logged at INFO level.
     * </p>
     * 
     * @param filePath the path to the CSV file to import
     */
    @Override
    public void importStudentsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line = br.readLine(); // skip header
            int count = 0;
            while ((line = br.readLine()) != null) {
                try {
                    String[] data = line.split(",");
                    ArrayList<String> courses = new ArrayList<>();
                    if (data.length > 4 && !data[4].isBlank()) {
                        String[] courseList = data[4].split(";");
                        for (String c : courseList) {
                            courses.add(c.trim());
                        }
                    }
                    Student s = new Student(data[0], Integer.parseInt(data[1]), Double.parseDouble(data[2]),
                            LocalDate.parse(data[3]), courses);
                    addStudent(s);
                    count++;
                } catch (Exception ex) {
                    logger.warn("Skipping invalid line: {}", line);
                }
            }
            logger.info("{} students imported successfully.", count);
        } catch (Exception e) {
            logger.error("Error importing students from CSV", e);
        }
    }

    /**
     * Adds a course enrollment for a student.
     * 
     * <p>
     * This method performs the following operations within a transaction:
     * </p>
     * <ol>
     * <li>Creates the course record if it doesn't exist (INSERT OR IGNORE)</li>
     * <li>Creates an enrollment record linking the student to the course</li>
     * </ol>
     * 
     * <p>
     * The initial enrollment grade is set to 0.0. If the enrollment already exists,
     * it is not duplicated due to the INSERT OR IGNORE statement.
     * </p>
     * 
     * @param studentID  the unique identifier of the student
     * @param courseCode the unique course code (e.g., "CS101")
     * @param courseName the descriptive course name
     * @param credits    the number of credit hours for the course
     */
    public void addCourseToStudent(String studentID, String courseCode, String courseName, int credits) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psCourse = conn.prepareStatement(
                    "INSERT OR IGNORE INTO courses(courseCode, courseName, credits) VALUES (?, ?, ?)");
                    PreparedStatement psEnroll = conn.prepareStatement(
                            "INSERT OR IGNORE INTO enrollments(studentID, courseCode, enrollmentGrade) VALUES (?, ?, ?)")) {

                psCourse.setString(1, courseCode);
                psCourse.setString(2, courseName);
                psCourse.setInt(3, credits);
                psCourse.executeUpdate();

                psEnroll.setString(1, studentID);
                psEnroll.setString(2, courseCode);
                psEnroll.setDouble(3, 0.0);
                psEnroll.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Transaction failed, rolled back", e);
            }

        } catch (SQLException e) {
            logger.error("Database error outside transaction during course addition", e);
        }
    }

    /**
     * Removes a course enrollment for a student.
     * 
     * <p>
     * This method deletes the enrollment record linking the student to the course.
     * The course record itself is not deleted, only the enrollment relationship.
     * </p>
     * 
     * @param studentID  the unique identifier of the student
     * @param courseCode the course code to remove from the student's enrollments
     */
    public void removeCourseFromStudent(String studentID, String courseCode) {
        String sql = "DELETE FROM enrollments WHERE studentID = ? AND courseCode = ?";

        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ps.setString(2, courseCode);
            ps.executeUpdate();

        } catch (SQLException e) {
            logger.error("Database error while removing course", e);
        }
    }

    /**
     * Checks if a student with the given ID exists in the database.
     * 
     * @param studentID the unique identifier to check
     * @return true if a student with this ID exists, false otherwise
     */
    public boolean studentExists(String studentID) {
        String sql = "SELECT 1 FROM students WHERE studentID = ?";
        try (Connection conn = getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            logger.error("Database error while checking student existence", e);
            return false;
        }
    }
}