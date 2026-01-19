package core;

import java.sql.Connection;
import java.sql.Statement;

/**
 * Utility class to initialize the test database schema.
 * Recreates the necessary table structures (students, courses, enrollments)
 * for the test environment.
 */
public class TestDatabaseInitializer {

    /**
     * Initializes the test database schema by creating required tables.
     * Tables created:
     * <ul>
     * <li>students: Stores student record information.</li>
     * <li>courses: Stores course definitions.</li>
     * <li>enrollments: Links students to courses with cascading deletions.</li>
     * </ul>
     */
    public static void initialize() {
        try (Connection conn = TestConnectionFactory.getConnection();
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
