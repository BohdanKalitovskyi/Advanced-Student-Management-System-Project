package core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory for creating database connections during test execution.
 * Provides access to a separate test database (typically an in-memory SQLite
 * database)
 * to ensure production data remains unaffected by tests.
 */
public class TestConnectionFactory {

    /**
     * The JDBC URL for the test database. Defaults to an in-memory database.
     */
    private static String backendUrl = "jdbc:sqlite::memory:";

    /**
     * Private constructor to prevent instantiation.
     */
    private TestConnectionFactory() {
    }

    /**
     * Sets the JDBC URL for the test database.
     *
     * @param url The JDBC URL to be used for test connections.
     */
    public static void setURL(String url) {
        backendUrl = url;
    }

    /**
     * Establishes a connection to the test database.
     *
     * @return A {@link Connection} object for the test database.
     * @throws SQLException If a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(backendUrl);
    }
}
