import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Student {
    private String name;
    private int age;
    private double grade;
    private final String studentID;
    private LocalDate enrollmentDate;
    private ArrayList<String> courses;

    public Student(String name, int age, double grade) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.studentID = UUID.randomUUID().toString();
        this.enrollmentDate = LocalDate.now();
        this.courses = new ArrayList<>();
    }

    public Student(String studentID,
                   String name,
                   int age,
                   double grade,
                   LocalDate enrollmentDate,
                   ArrayList<String> courses) {

        this.studentID = studentID;
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.enrollmentDate = enrollmentDate;
        this.courses = courses;
    }

    public Student(String name, int age, double grade, LocalDate enrollmentDate, ArrayList<String> courses) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.studentID = UUID.randomUUID().toString();
        this.enrollmentDate = enrollmentDate != null ? enrollmentDate : LocalDate.now();
        this.courses = courses != null ? courses : new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || !name.matches("[a-zA-Z\\s-]+")) {
            throw new IllegalArgumentException("Invalid name. Only letters, spaces, and hyphens are allowed.");
        }
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        if (age < 18 || age > 100) {
            throw new IllegalArgumentException("Age must be between 18 and 100.");
        }
        this.age = age;
    }

    public double getGrade() {
        return grade;
    }

    public void setGrade(double grade) {
        if (grade < 0.0 || grade > 100.0) {
            throw new IllegalArgumentException("Grade must be between 0.0 and 100.0.");
        }
        this.grade = Math.round(grade * 100.0) / 100.0;
    }

    public String getStudentID() {
        return studentID;
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public ArrayList<String> getCourses() {
        return courses;
    }

    public void addCourse(String course) {
        if (!courses.contains(course)) {
            courses.add(course);
        }
    }

    public void removeCourse(String course) {
        courses.remove(course);
    }

    public String displayInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Student ID: ").append(studentID).append("\n");
        sb.append("Name: ").append(name).append("\n");
        sb.append("Age: ").append(age).append("\n");
        sb.append("Grade: ").append(grade).append("\n");
        sb.append("Enrollment Date: ").append(enrollmentDate).append("\n");
        sb.append("Courses: ");
        sb.append(String.join(", ", courses));
        return sb.toString();
    }

    public double calculateGPA() {
        // normalized to 4.0 scale
        return (grade / 100.0) * 4.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;
        Student student = (Student) o;
        return studentID.equals(student.studentID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentID);
    }
}
