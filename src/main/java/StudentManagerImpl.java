import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;

public class StudentManagerImpl implements StudentManager {

    private static StudentManagerImpl instance;

    private StudentManagerImpl() {
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
        if (studentExists(student.getStudentID())) {
            System.out.println("Student with ID " + student.getStudentID() + " already exists!");
            return;
        }

        String sql = """
        INSERT INTO students (studentID, name, age, grade, enrollmentDate)
        VALUES (?, ?, ?, ?, ?)
    """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, student.getStudentID());
            ps.setString(2, student.getName());
            ps.setInt(3, student.getAge());
            ps.setDouble(4, student.getGrade());
            ps.setString(5, student.getEnrollmentDate().toString());

            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void removeStudent(String studentID) {
        String sql = "DELETE FROM students WHERE studentID = ?";

        try (Connection conn = ConnectionFactory.getConnection();
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

        try (Connection conn = ConnectionFactory.getConnection();
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

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Student s = new Student(
                        rs.getString("studentID"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("grade"),
                        LocalDate.parse(rs.getString("enrollmentDate")),
                        new ArrayList<>()
                );

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
        WHERE s.name LIKE ? OR s.studentID LIKE ? OR c.courseCode LIKE ?
    """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String studentID = rs.getString("studentID");
                Student s = new Student(
                        studentID,
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("grade"),
                        LocalDate.parse(rs.getString("enrollmentDate")),
                        new ArrayList<>()
                );

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

            writer.println("name,age,grade,enrollmentDate");

            for (Student s : displayAllStudents()) {
                writer.printf(
                        "%s,%d,%.2f,%s%n",
                        s.getName(),
                        s.getAge(),
                        s.getGrade(),
                        s.getEnrollmentDate()
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
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
                    Student s = new Student(data[0], Integer.parseInt(data[1]), Double.parseDouble(data[2]), LocalDate.parse(data[3]), new ArrayList<>());
                    addStudent(s);
                    count++;
                } catch (Exception ex) {
                    System.err.println("Skipping invalid line: " + line);
                }
            }
            System.out.println(count + " students imported successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addCourseToStudent(String studentID, String courseCode, String courseName, int credits) {
        try (Connection conn = ConnectionFactory.getConnection()) {
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
                psEnroll.setDouble(3, 0.0); // початкова оцінка
                psEnroll.executeUpdate();

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Database error: " + e.getMessage());
                e.printStackTrace();
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void removeCourseFromStudent(String studentID, String courseCode) {
        String sql = "DELETE FROM enrollments WHERE studentID = ? AND courseCode = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ps.setString(2, courseCode);
            ps.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean studentExists(String studentID) {
        String sql = "SELECT 1 FROM students WHERE studentID = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}