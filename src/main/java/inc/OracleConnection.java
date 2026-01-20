package inc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class OracleConnection {
    private static String URL = "jdbc:oracle:thin:@//localhost:1521/EE.oracle.docker";
    private static String USER = "vacance"; 
    private static String PASSWORD = "vac"; 

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Connexion Oracle réussie.");
            conn.setAutoCommit(false);
            return conn;
        } catch (SQLException e) {
            System.err.println("Erreur de connexion à Oracle : " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}