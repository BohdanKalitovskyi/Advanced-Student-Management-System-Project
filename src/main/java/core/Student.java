package core;

import core.exceptions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Represents a student in the Student Management System.
 * 
 * <p>
 * This class encapsulates all information about a student including personal
 * details,
 * academic performance, enrollment information, and course registrations. Each
 * student
 * is uniquely identified by a UUID-based student ID.
 * </p>
 * 
 * <p>
 * The class enforces validation rules for student data:
 * <ul>
 * <li>Name must contain only letters, spaces, and hyphens</li>
 * <li>Age must be between 18 and 100 (inclusive)</li>
 * <li>Grade must be between 0.0 and 100.0 (inclusive)</li>
 * </ul>
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class Student {
    /**
     * The student's full name.
     * Must contain only letters, spaces, and hyphens.
     */
    private String name;

    /**
     * The student's age in years.
     * Must be between 18 and 100 (inclusive).
     */
    private int age;

    /**
     * The student's overall grade as a percentage.
     * Must be between 0.0 and 100.0 (inclusive), rounded to 2 decimal places.
     */
    private double grade;

    /**
     * Unique identifier for the student.
     * Generated using UUID and cannot be changed after creation.
     */
    private final String studentID;

    /**
     * The date when the student enrolled in the system.
     * Defaults to the current date if not specified.
     */
    private LocalDate enrollmentDate;

    /**
     * List of course codes the student is enrolled in.
     * Stored as course code strings (e.g., "CS101", "MATH201").
     */
    private ArrayList<String> courses;

    /**
     * Constructs a new Student with basic information.
     * 
     * <p>
     * This constructor creates a student with a randomly generated UUID as the
     * student ID,
     * sets the enrollment date to the current date, and initializes an empty course
     * list.
     * </p>
     * 
     * @param name  the student's full name (letters, spaces, and hyphens only)
     * @param age   the student's age (must be 18-100)
     * @param grade the student's overall grade (must be 0.0-100.0)
     */
    public Student(String name, int age, double grade) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.studentID = UUID.randomUUID().toString();
        this.enrollmentDate = LocalDate.now();
        this.courses = new ArrayList<>();
    }

    /**
     * Constructs a Student with all fields specified, including a custom student
     * ID.
     * 
     * <p>
     * This constructor is typically used when loading students from a database
     * where the student ID already exists.
     * </p>
     * 
     * @param studentID      the unique identifier for the student
     * @param name           the student's full name
     * @param age            the student's age
     * @param grade          the student's overall grade
     * @param enrollmentDate the date the student enrolled
     * @param courses        list of course codes the student is enrolled in
     */
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

    /**
     * Constructs a Student with specified details and auto-generated student ID.
     * 
     * <p>
     * This constructor generates a new UUID for the student ID and provides
     * default values for null parameters (current date for enrollment, empty list
     * for courses).
     * </p>
     * 
     * @param name           the student's full name
     * @param age            the student's age
     * @param grade          the student's overall grade
     * @param enrollmentDate the enrollment date (uses current date if null)
     * @param courses        list of course codes (uses empty list if null)
     */
    public Student(String name, int age, double grade, LocalDate enrollmentDate, ArrayList<String> courses) {
        this.name = name;
        this.age = age;
        this.grade = grade;
        this.studentID = UUID.randomUUID().toString();
        this.enrollmentDate = enrollmentDate != null ? enrollmentDate : LocalDate.now();
        this.courses = courses != null ? courses : new ArrayList<>();
    }

    /**
     * Returns the student's name.
     * 
     * @return the student's full name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the student's name after validation.
     * 
     * <p>
     * The name must contain only letters (a-z, A-Z), spaces, and hyphens.
     * Any other characters will cause validation to fail.
     * </p>
     * 
     * @param name the new name for the student
     * @throws InvalidNameException if the name is null or contains invalid
     *                              characters
     */
    public void setName(String name) {
        if (name == null || !name.matches("[a-zA-Z\\s-]+")) {
            throw new InvalidNameException("Only letters, spaces, hyphens allowed");
        }
        this.name = name;
    }

    /**
     * Returns the student's age.
     * 
     * @return the student's age in years
     */
    public int getAge() {
        return age;
    }

    /**
     * Sets the student's age after validation.
     * 
     * <p>
     * Age must be between 18 and 100 (inclusive) to be valid.
     * This ensures students are of appropriate age for the system.
     * </p>
     * 
     * @param age the new age for the student
     * @throws InvalidAgeException if the age is less than 18 or greater than 100
     */
    public void setAge(int age) {
        if (age < 18 || age > 100) {
            throw new InvalidAgeException("Age must be 18-100");
        }
        this.age = age;
    }

    /**
     * Returns the student's overall grade.
     * 
     * @return the student's grade as a percentage (0.0-100.0)
     */
    public double getGrade() {
        return grade;
    }

    /**
     * Sets the student's grade after validation and rounding.
     * 
     * <p>
     * Grade must be between 0.0 and 100.0 (inclusive). The grade is automatically
     * rounded to 2 decimal places for consistency.
     * </p>
     * 
     * @param grade the new grade for the student (0.0-100.0)
     * @throws InvalidGradeException if the grade is less than 0.0 or greater than
     *                               100.0
     */
    public void setGrade(double grade) {
        if (grade < 0.0 || grade > 100.0) {
            throw new InvalidGradeException("Grade must be 0-100");
        }
        this.grade = Math.round(grade * 100.0) / 100.0;
    }

    /**
     * Returns the student's unique identifier.
     * 
     * <p>
     * The student ID is immutable and generated using UUID when the student is
     * created.
     * </p>
     * 
     * @return the student's unique ID
     */
    public String getStudentID() {
        return studentID;
    }

    /**
     * Returns the date when the student enrolled.
     * 
     * @return the enrollment date
     */
    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    /**
     * Sets the student's enrollment date.
     * 
     * @param enrollmentDate the new enrollment date
     */
    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    /**
     * Returns the list of courses the student is enrolled in.
     * 
     * @return list of course codes (e.g., "CS101", "MATH201")
     */
    public ArrayList<String> getCourses() {
        return courses;
    }

    /**
     * Adds a course to the student's enrollment list.
     * 
     * <p>
     * Duplicate courses are automatically prevented - if the course is already
     * in the list, it will not be added again.
     * </p>
     * 
     * @param course the course code to add (e.g., "CS101")
     */
    public void addCourse(String course) {
        if (!courses.contains(course)) {
            courses.add(course);
        }
    }

    /**
     * Removes a course from the student's enrollment list.
     * 
     * @param course the course code to remove
     */
    public void removeCourse(String course) {
        courses.remove(course);
    }

    /**
     * Generates a formatted string containing all student information.
     * 
     * <p>
     * The output includes student ID, name, age, grade, enrollment date,
     * and a comma-separated list of enrolled courses.
     * </p>
     * 
     * @return a multi-line string with formatted student information
     */
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

    /**
     * Calculates the student's GPA on a 4.0 scale.
     * 
     * <p>
     * The GPA is calculated by normalizing the percentage grade (0-100)
     * to the standard 4.0 scale used in many educational institutions.
     * </p>
     * 
     * @return the GPA value (0.0-4.0)
     */
    public double calculateGPA() {
        // normalized to 4.0 scale
        return (grade / 100.0) * 4.0;
    }

    /**
     * Compares this student to another object for equality.
     * 
     * <p>
     * Two students are considered equal if they have the same student ID.
     * All other fields are ignored in the equality comparison.
     * </p>
     * 
     * @param o the object to compare with
     * @return true if the objects are equal (same student ID), false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Student))
            return false;
        Student student = (Student) o;
        return studentID.equals(student.studentID);
    }

    /**
     * Returns a hash code value for this student.
     * 
     * <p>
     * The hash code is based solely on the student ID to maintain
     * consistency with the equals() method.
     * </p>
     * 
     * @return a hash code value for this student
     */
    @Override
    public int hashCode() {
        return Objects.hash(studentID);
    }
}
