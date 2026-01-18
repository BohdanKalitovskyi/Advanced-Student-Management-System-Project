package core;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

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
