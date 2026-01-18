package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of StudentManager using SQLite database.
 * Changes:
 * - Added SLF4J logging.
 * - Transactions for critical operations.
 */
public class StudentManagerImpl implements StudentManager {
    private static final Logger logger = LoggerFactory.getLogger(StudentManagerImpl.class);

    private static StudentManagerImpl instance;

    protected Connection getConnection() throws SQLException {
        return ConnectionFactory.getConnection();
    }

    protected StudentManagerImpl() {
        initializeDatabase();
    }

    protected void initializeDatabase() {
        DatabaseInitializer.initialize();
    }

    public static synchronized StudentManagerImpl getInstance() {
        if (instance == null) {
            instance = new StudentManagerImpl();
        }
        return instance;
    }

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
                            // Ensure course exists
                            coursePs.setString(1, course);
                            coursePs.setString(2, "Course " + course); // Default name
                            coursePs.setInt(3, 4); // Default credits
                            coursePs.executeUpdate();

                            // Enroll student
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

    @Override
    public ArrayList<Student> displayAllStudents() {
        ArrayList<Student> students = new ArrayList<>();

        String sql = "SELECT * FROM students";

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
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }

        students.sort(Comparator.comparing(Student::getName));

        return students;
    }

    @Override
    public double calculateAverageGrade() {
        return displayAllStudents().stream()
                .mapToDouble(Student::getGrade)
                .filter(g -> g >= 0 && g <= 100)
                .average()
                .orElse(0.0);
    }

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
                psEnroll.setDouble(3, 0.0); // initial grade
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