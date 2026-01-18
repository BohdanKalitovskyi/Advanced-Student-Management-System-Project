import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final String URL = "jdbc:sqlite:/Users/admin/Documents/tasks/Proj/students.db";

    private ConnectionFactory() {}

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}