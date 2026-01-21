package core;

import java.util.ArrayList;

/**
 * Interface defining the contract for student management operations.
 * 
 * <p>
 * This interface provides the core functionality for managing students in the
 * system,
 * including CRUD operations, searching, grade calculations, and CSV
 * import/export capabilities.
 * </p>
 * 
 * <p>
 * Implementations of this interface should handle database persistence,
 * validation,
 * and error handling for all student-related operations.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public interface StudentManager {

    /**
     * Adds a new student to the system.
     * 
     * <p>
     * The student's information is validated and persisted to the database.
     * If a student with the same ID already exists, the operation should be skipped
     * or handled gracefully.
     * </p>
     * 
     * @param student the student object to add (must not be null)
     */
    void addStudent(Student student);

    /**
     * Removes a student from the system by their unique ID.
     * 
     * <p>
     * This operation also removes all associated course enrollments due to
     * cascade delete constraints in the database.
     * </p>
     * 
     * @param studentID the unique identifier of the student to remove
     */
    void removeStudent(String studentID);

    /**
     * Updates an existing student's information.
     * 
     * <p>
     * The student identified by studentID will have their information replaced
     * with the data from updatedStudent. The student ID itself cannot be changed.
     * </p>
     * 
     * @param studentID      the unique identifier of the student to update
     * @param updatedStudent the student object containing the new information
     */
    void updateStudent(String studentID, Student updatedStudent);

    /**
     * Retrieves all students from the system.
     * 
     * <p>
     * Students are returned sorted alphabetically by name. Each student object
     * includes their enrolled courses loaded from the database.
     * </p>
     * 
     * @return an ArrayList containing all students in the system
     */
    ArrayList<Student> displayAllStudents();

    /**
     * Retrieves all students from the system with custom sorting.
     * 
     * @param sortBy the field to sort by (e.g., "name", "grade", "age")
     * @return an ArrayList containing all students in the system sorted by the
     *         specified field
     */
    ArrayList<Student> displayAllStudents(String sortBy);

    /**
     * Calculates the average grade across all students.
     * 
     * <p>
     * Only valid grades (0.0-100.0) are included in the calculation.
     * If no students exist, returns 0.0.
     * </p>
     * 
     * @return the average grade as a percentage (0.0-100.0)
     */
    double calculateAverageGrade();

    /**
     * Calculates the average grade for a specific course/group.
     * 
     * @param courseCode the unique identifier of the course
     * @return the average grade of students enrolled in the specified course
     */
    double calculateAverageGrade(String courseCode);

    /**
     * Searches for students matching the given query.
     * 
     * <p>
     * The search is performed across multiple fields including:
     * student name, student ID, age, grade, course codes, and course names.
     * The search is case-insensitive and supports partial matches.
     * </p>
     * 
     * @param query the search term to match against student data
     * @return an ArrayList of students matching the search criteria
     */
    ArrayList<Student> searchStudents(String query);

    /**
     * Exports all students to a CSV file.
     * 
     * <p>
     * The CSV file includes headers and contains student information in the format:
     * name, age, grade, enrollmentDate, courses (semicolon-separated)
     * </p>
     * 
     * @param filePath the path where the CSV file should be created
     */
    void exportStudentsToCSV(String filePath);

    /**
     * Imports students from a CSV file.
     * 
     * <p>
     * The CSV file must have headers and follow the format:
     * name, age, grade, enrollmentDate, courses (semicolon-separated).
     * Invalid lines are skipped with a warning logged.
     * </p>
     * 
     * @param filePath the path to the CSV file to import
     */
    void importStudentsFromCSV(String filePath);
}