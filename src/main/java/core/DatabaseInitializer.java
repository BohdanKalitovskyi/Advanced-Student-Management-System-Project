package core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for initializing the database schema.
 * 
 * <p>
 * This class is responsible for creating the necessary database tables if they
 * don't
 * already exist. It creates three main tables:
 * </p>
 * <ul>
 * <li><b>students</b>: Stores student information (ID, name, age, grade,
 * enrollment date)</li>
 * <li><b>courses</b>: Stores course information (code, name, credits)</li>
 * <li><b>enrollments</b>: Junction table linking students to courses with
 * grades</li>
 * </ul>
 * 
 * <p>
 * The schema includes appropriate constraints, foreign keys, and cascade delete
 * behavior to maintain referential integrity.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public class DatabaseInitializer {

    /**
     * Initializes the database schema by creating all necessary tables.
     * 
     * <p>
     * This method creates three tables if they don't already exist:
     * </p>
     * 
     * <p>
     * <b>students table:</b>
     * </p>
     * <ul>
     * <li>studentID (TEXT, PRIMARY KEY): Unique student identifier</li>
     * <li>name (TEXT, NOT NULL): Student's full name</li>
     * <li>age (INTEGER, CHECK 18-100): Student's age with validation</li>
     * <li>grade (REAL, CHECK 0-100): Student's overall grade percentage</li>
     * <li>enrollmentDate (DATE): Date the student enrolled</li>
     * </ul>
     * 
     * <p>
     * <b>courses table:</b>
     * </p>
     * <ul>
     * <li>courseCode (TEXT, PRIMARY KEY): Unique course identifier</li>
     * <li>courseName (TEXT): Descriptive course name</li>
     * <li>credits (INTEGER): Number of credit hours</li>
     * </ul>
     * 
     * <p>
     * <b>enrollments table:</b>
     * </p>
     * <ul>
     * <li>studentID (TEXT, FOREIGN KEY): References students table</li>
     * <li>courseCode (TEXT, FOREIGN KEY): References courses table</li>
     * <li>enrollmentGrade (REAL): Grade for this specific course enrollment</li>
     * <li>PRIMARY KEY (studentID, courseCode): Composite key</li>
     * <li>ON DELETE CASCADE: Automatically removes enrollments when student/course
     * is deleted</li>
     * </ul>
     * 
     * <p>
     * If any SQL errors occur during table creation, they are printed to stderr.
     * </p>
     */
    public static void initialize() {
        try (Connection conn = ConnectionFactory.getConnection();
                Statement stmt = conn.createStatement()) {

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS students (
                            studentID TEXT PRIMARY KEY,
                            name TEXT NOT NULL,
                            age INTEGER CHECK(age >= 18 AND age <= 100),
                            grade REAL CHECK(grade >= 0 AND grade <= 100),
                            enrollmentDate DATE
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS courses (
                            courseCode TEXT PRIMARY KEY,
                            courseName TEXT,
                            credits INTEGER
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS enrollments (
                            studentID TEXT,
                            courseCode TEXT,
                            enrollmentGrade REAL,
                            PRIMARY KEY (studentID, courseCode),
                            FOREIGN KEY (studentID) REFERENCES students(studentID) ON DELETE CASCADE,
                            FOREIGN KEY (courseCode) REFERENCES courses(courseCode) ON DELETE CASCADE
                        )
                    """);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
