package src.logic.data.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private Connection connection;
    private String dbURL;
    private String userName;
    private String password;

    public DBConnection(String dbURL, String userName, String password) throws SQLException {
        connection = DriverManager.getConnection(dbURL, userName, password);
    }

    public Connection getConnection() {
        return connection;
    }
}
