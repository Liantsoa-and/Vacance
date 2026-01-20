package inc;

import java.sql.Connection;
import java.sql.DriverManager;

public class PostgresConnection {
    private static final String URL = "jdbc:postgresql://localhost:5432/vacance";
    private static final String USER = "lili";
    private static final String PASSWORD = "lili";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            // System.out.println("Connexion PostgreSQL réussie.");
            conn.setAutoCommit(false);
            return conn;
        } catch (Exception e) {
            System.err.println("Erreur de connexion à PostgreSQL :");
            e.printStackTrace();
            return null;
        }
    }

}