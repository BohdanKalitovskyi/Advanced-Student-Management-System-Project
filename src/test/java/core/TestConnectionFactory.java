package core;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class TestConnectionFactory {

    private static String backendUrl = "jdbc:sqlite::memory:";

    private TestConnectionFactory() {
    }

    public static void setURL(String url) {
        backendUrl = url;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(backendUrl);
    }
}
