package foodpack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL     = "jdbc:mysql://localhost:3306/foodbank";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "root1234";

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found. Add the JAR to Build Path.", e);
        }
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }
}