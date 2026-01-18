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
        String sql = "DELETE FROM students WHERE studentID = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, studentID);
            ps.executeUpdate();

        } catch (SQLException e) {
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

                students.add(s);
            }

        } catch (SQLException e) {
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
        SELECT * FROM students
        WHERE name LIKE ? OR studentID LIKE ?
    """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String pattern = "%" + query + "%";
            ps.setString(1, pattern);
            ps.setString(2, pattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Student s = new Student(
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getDouble("grade"),
                        LocalDate.parse(rs.getString("enrollmentDate")),
                        new ArrayList<>()
                );
                result.add(s);
            }

        } catch (SQLException e) {
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

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                Student s = new Student(
                        data[0],
                        Integer.parseInt(data[1]),
                        Double.parseDouble(data[2]),
                        LocalDate.parse(data[3]),
                        new ArrayList<>()
                );

                addStudent(s);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}