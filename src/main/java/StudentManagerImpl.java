import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

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
            e.printStackTrace();
        }
    }

    @Override
    public void removeStudent(String studentID) {

    }

    @Override
    public void updateStudent(String studentID, Student updatedStudent) {

    }

    @Override
    public ArrayList<Student> displayAllStudents() {
        return null;
    }

    @Override
    public double calculateAverageGrade() {
        return 0;
    }

    @Override
    public ArrayList<Student> searchStudents(String query) {
        return null;
    }

    @Override
    public void exportStudentsToCSV(String filePath) {

    }

    @Override
    public void importStudentsFromCSV(String filePath) {

    }
}