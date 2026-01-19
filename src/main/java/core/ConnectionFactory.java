package core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory class for creating database connections.
 * 
 * <p>
 * This class implements the Factory pattern to provide a centralized way to
 * obtain
 * database connections. It uses SQLite as the database engine with a local
 * file-based
 * database (students.db).
 * </p>
 * 
 * <p>
 * The class is designed as a utility class with a private constructor to
 * prevent
 * instantiation, providing only static methods for connection creation.
 * </p>
 * 
 * @author Student Management System Team
 * @version 1.0
 * @since 1.0
 */
public final class ConnectionFactory {

    /**
     * JDBC URL for the SQLite database.
     * Points to a local file named "students.db" in the project root directory.
     */
    private static final String URL = "jdbc:sqlite:students.db";

    /**
     * Private constructor to prevent instantiation.
     * This class should only be used through its static methods.
     */
    private ConnectionFactory() {
    }

    /**
     * Creates and returns a new database connection.
     * 
     * <p>
     * Each call to this method creates a new connection to the SQLite database.
     * Callers are responsible for closing the connection when finished to prevent
     * resource leaks.
     * </p>
     * 
     * @return a new Connection object to the students database
     * @throws SQLException if a database access error occurs or the URL is invalid
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}