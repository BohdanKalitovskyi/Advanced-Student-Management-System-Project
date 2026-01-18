import java.util.ArrayList;

public interface StudentManager {

    void addStudent(Student student);

    void removeStudent(String studentID);

    void updateStudent(String studentID, Student updatedStudent);

    ArrayList<Student> displayAllStudents();

    double calculateAverageGrade();

    ArrayList<Student> searchStudents(String query);

    void exportStudentsToCSV(String filePath);

    void importStudentsFromCSV(String filePath);
}