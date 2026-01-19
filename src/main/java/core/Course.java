package core;

/**
 * Represents a course in the Student Management System.
 * 
 * <p>
 * A course contains basic information including a unique course code,
 * descriptive name, and credit hours. Courses can be enrolled in by students
 * through the enrollment system.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class Course {
    /**
     * Unique identifier for the course (e.g., "CS101", "MATH201").
     */
    private String courseCode;

    /**
     * Descriptive name of the course (e.g., "Introduction to Computer Science").
     */
    private String courseName;

    /**
     * Number of credit hours for the course.
     */
    private int credits;

    /**
     * Constructs a new Course with the specified details.
     * 
     * @param courseCode the unique course code
     * @param courseName the descriptive course name
     * @param credits    the number of credit hours
     */
    public Course(String courseCode, String courseName, int credits) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.credits = credits;
    }

    /**
     * Returns the course code.
     * 
     * @return the unique course identifier
     */
    public String getCourseCode() {
        return courseCode;
    }

    /**
     * Returns the course name.
     * 
     * @return the descriptive course name
     */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Returns the number of credit hours.
     * 
     * @return the credit hours for this course
     */
    public int getCredits() {
        return credits;
    }
}