package dao;

import inc.OracleConnection;
import model.Point;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PointDAO {
    public List<Point> getAll() {
        List<Point> listePoints = new ArrayList<>();
        
        String sql = "SELECT * FROM POINT ORDER BY nom";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            // Parcours des résultats
            while (rs.next()) {
                // On récupère les colonnes par leur nom dans la BDD
                int id = rs.getInt("id");
                String nom = rs.getString("nom");

                // On crée l'objet Java
                Point point = new Point(id, nom);

                // On l'ajoute à la liste
                listePoints.add(point);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans PointDAO.getAll() : " + e.getMessage());
            e.printStackTrace();
        }

        return listePoints;
    }

    /**
     * Exemple de méthode pour récupérer un point par son ID (utile plus tard)
     */
    public Point getById(int idCherche) {
        Point point = null;
        String sql = "SELECT id, nom FROM POINT WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            // On remplace le '?' par l'ID demandé (protection contre injection SQL)
            stmt.setInt(1, idCherche);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    point = new Point(rs.getInt("id"), rs.getString("nom"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return point;
    }
}