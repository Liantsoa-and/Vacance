package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Chemin;
import inc.OracleConnection;

public class CheminDAO {
    public List<Chemin> getAll() throws SQLException {
        List<Chemin> chemins = new ArrayList<>();

        String sql = "SELECT * FROM CHEMIN ORDER BY id";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                int pointDebutId = rs.getInt("point_debut_id");
                int pointFinId = rs.getInt("point_fin_id");
                double distanceKm = rs.getDouble("distance_km");
                double largeurM = rs.getDouble("largeur_m");
                String sens = rs.getString("sens");

                Chemin chemin = new Chemin(id, nom, pointDebutId, pointFinId, distanceKm, largeurM, sens);
                chemins.add(chemin);
            }
        }
        return chemins;
    }

    public Chemin getById(int idCherche) throws SQLException {
        Chemin chemin = null;
        String sql = "SELECT * FROM CHEMIN WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCherche);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String nom = rs.getString("nom");
                    int pointDebutId = rs.getInt("point_debut_id");
                    int pointFinId = rs.getInt("point_fin_id");
                    double distanceKm = rs.getDouble("distance_km");
                    double largeurM = rs.getDouble("largeur_m");
                    String sens = rs.getString("sens");

                    chemin = new Chemin(id, nom, pointDebutId, pointFinId, distanceKm, largeurM, sens);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return chemin;
    }

}
