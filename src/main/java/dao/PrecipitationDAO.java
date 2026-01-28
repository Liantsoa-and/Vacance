package dao;

import model.Precipitation;
import inc.OracleConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PrecipitationDAO {
    private Connection connection;

    public PrecipitationDAO() {
        this.connection = OracleConnection.getConnection();
    }

    /**
     * Récupère toutes les zones de précipitation d'un chemin donné
     */
    public List<Precipitation> findByCheminId(int cheminId) throws SQLException {
        List<Precipitation> precipitations = new ArrayList<>();
        String sql = "SELECT id, chemin_id, debut_km, fin_km, niveau_mm FROM PRECIPITATION WHERE chemin_id = ? ORDER BY debut_km";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cheminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Precipitation p = new Precipitation(
                            rs.getInt("id"),
                            rs.getInt("chemin_id"),
                            rs.getDouble("debut_km"),
                            rs.getDouble("fin_km"),
                            rs.getDouble("niveau_mm"));
                    precipitations.add(p);
                }
            }
        }

        return precipitations;
    }

    /**
     * Trouve la zone de précipitation contenant un point kilométrique donné
     */
    public Precipitation findByCheminAndKm(int cheminId, double km) throws SQLException {
        String sql = "SELECT id, chemin_id, debut_km, fin_km, niveau_mm FROM PRECIPITATION " +
                "WHERE chemin_id = ? AND debut_km <= ? AND fin_km > ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cheminId);
            stmt.setDouble(2, km);
            stmt.setDouble(3, km);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Precipitation(
                            rs.getInt("id"),
                            rs.getInt("chemin_id"),
                            rs.getDouble("debut_km"),
                            rs.getDouble("fin_km"),
                            rs.getDouble("niveau_mm"));
                }
            }
        }

        return null; // Aucune zone trouvée = 0mm par défaut
    }

    /**
     * Ajoute une nouvelle zone de précipitation
     */
    public boolean create(Precipitation p) throws SQLException {
        // Vérifier d'abord qu'il n'y a pas de chevauchement
        if (!validateNonOverlapping(p)) {
            throw new SQLException("Chevauchement détecté avec une zone existante");
        }

        String sql = "INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm) VALUES (SEQ_PRECIPITATION.NEXTVAL, ?, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, p.getCheminId());
            stmt.setDouble(2, p.getDebutKm());
            stmt.setDouble(3, p.getFinKm());
            stmt.setDouble(4, p.getNiveauMm());

            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Modifie une zone de précipitation existante
     */
    public boolean update(Precipitation p) throws SQLException {
        // Vérifier qu'il n'y a pas de chevauchement avec les autres zones
        if (!validateNonOverlapping(p)) {
            throw new SQLException("Chevauchement détecté avec une zone existante");
        }

        String sql = "UPDATE PRECIPITATION SET chemin_id = ?, debut_km = ?, fin_km = ?, niveau_mm = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, p.getCheminId());
            stmt.setDouble(2, p.getDebutKm());
            stmt.setDouble(3, p.getFinKm());
            stmt.setDouble(4, p.getNiveauMm());
            stmt.setInt(5, p.getId());

            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Supprime une zone de précipitation
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM PRECIPITATION WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Vérifie qu'une zone de précipitation ne chevauche pas avec les zones
     * existantes
     */
    public boolean validateNonOverlapping(Precipitation p) throws SQLException {
        String sql = "SELECT COUNT(*) FROM PRECIPITATION WHERE chemin_id = ? AND id != ? AND " +
                "((debut_km < ? AND fin_km > ?) OR (debut_km < ? AND fin_km > ?) OR " +
                "(debut_km >= ? AND fin_km <= ?))";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, p.getCheminId());
            stmt.setInt(2, p.getId()); // Pour exclure la zone actuelle lors de la modification
            stmt.setDouble(3, p.getFinKm()); // Vérifier si une zone commence avant notre fin
            stmt.setDouble(4, p.getDebutKm()); // et finit après notre début
            stmt.setDouble(5, p.getDebutKm()); // Vérifier si une zone commence avant notre début
            stmt.setDouble(6, p.getFinKm()); // et finit après notre fin
            stmt.setDouble(7, p.getDebutKm()); // Vérifier si une zone est complètement incluse
            stmt.setDouble(8, p.getFinKm()); // dans notre zone

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0; // Aucun chevauchement trouvé
                }
            }
        }

        return true;
    }

    /**
     * Récupère toutes les zones de précipitation
     */
    public List<Precipitation> findAll() throws SQLException {
        List<Precipitation> precipitations = new ArrayList<>();
        String sql = "SELECT id, chemin_id, debut_km, fin_km, niveau_mm FROM PRECIPITATION ORDER BY chemin_id, debut_km";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Precipitation p = new Precipitation(
                        rs.getInt("id"),
                        rs.getInt("chemin_id"),
                        rs.getDouble("debut_km"),
                        rs.getDouble("fin_km"),
                        rs.getDouble("niveau_mm"));
                precipitations.add(p);
            }
        }

        return precipitations;
    }
}