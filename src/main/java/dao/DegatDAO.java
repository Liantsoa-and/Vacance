package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Degat;
import inc.OracleConnection;

public class DegatDAO {

    /**
     * Récupère tous les dégâts
     */
    public List<Degat> getAll() throws SQLException {
        List<Degat> degats = new ArrayList<>();
        String sql = "SELECT * FROM DEGAT ORDER BY chemin_id, point_km";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Degat degat = new Degat();
                degat.setId(rs.getInt("id"));
                degat.setCheminId(rs.getInt("chemin_id"));
                degat.setPointKm(rs.getDouble("point_km"));
                degat.setSurfaceM2(rs.getDouble("surface_m2"));
                degat.setProfondeurM(rs.getDouble("profondeur_m"));

                degats.add(degat);
            }
        }
        return degats;
    }

    /**
     * Récupère les dégâts pour un chemin spécifique
     */
    public List<Degat> getParChemin(int cheminId) throws SQLException {
        List<Degat> degats = new ArrayList<>();
        String sql = "SELECT * FROM DEGAT WHERE chemin_id = ? ORDER BY point_km";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cheminId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Degat degat = new Degat();
                    degat.setId(rs.getInt("id"));
                    degat.setCheminId(rs.getInt("chemin_id"));
                    degat.setPointKm(rs.getDouble("point_km"));
                    degat.setSurfaceM2(rs.getDouble("surface_m2"));
                    degat.setProfondeurM(rs.getDouble("profondeur_m"));

                    degats.add(degat);
                }
            }
        }
        return degats;
    }

    /**
     * Insère un nouveau dégât
     * 
     * @return l'ID généré
     */
    public int inserer(Degat degat) throws SQLException {
        if (degat.getCheminId() == null) {
            throw new SQLException("Le chemin est obligatoire");
        }
        if (degat.getSurfaceM2() <= 0) {
            throw new SQLException("La surface doit être > 0");
        }
        if (degat.getProfondeurM() < 0) {
            throw new SQLException("La profondeur doit être >= 0");
        }

        String sql = "INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) " +
                "VALUES (SEQ_DEGAT.NEXTVAL, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "id" })) {

            stmt.setInt(1, degat.getCheminId());
            stmt.setDouble(2, degat.getPointKm());
            stmt.setDouble(3, degat.getSurfaceM2());
            stmt.setDouble(4, degat.getProfondeurM());

            int affectes = stmt.executeUpdate();

            if (affectes > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Échec de l'insertion du dégât");
    }

    /**
     * Supprime un dégât
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM DEGAT WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectes = stmt.executeUpdate();

            if (affectes == 0) {
                throw new SQLException("Dégât introuvable");
            }
        }
    }

    /**
     * Met à jour le point kilométrique d'un dégât
     */
    public void mettreAJourPointKm(int id, double nouveauPointKm) throws SQLException {
        if (nouveauPointKm < 0) {
            throw new SQLException("Le point kilométrique doit être >= 0");
        }

        String sql = "UPDATE DEGAT SET point_km = ? WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, nouveauPointKm);
            stmt.setInt(2, id);

            int affectes = stmt.executeUpdate();
            if (affectes == 0) {
                throw new SQLException("Dégât introuvable");
            }
        }
    }

    /**
     * Récupère un dégât par son ID
     */
    public Degat getById(int id) throws SQLException {
        String sql = "SELECT * FROM DEGAT WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Degat degat = new Degat();
                    degat.setId(rs.getInt("id"));
                    degat.setCheminId(rs.getInt("chemin_id"));
                    degat.setPointKm(rs.getDouble("point_km"));
                    degat.setSurfaceM2(rs.getDouble("surface_m2"));
                    degat.setProfondeurM(rs.getDouble("profondeur_m"));
                    return degat;
                } else {
                    return null;
                }
            }
        }
    }

    /**
     * Récupère tous les dégâts d'un chemin spécifique
     * Cette méthode est utilisée pour obtenir la liste complète des dégâts d'un
     * chemin
     */
    public List<Degat> getDegatsByCheminId(int cheminId) throws SQLException {
        return getParChemin(cheminId);
    }

    /**
     * Récupère les dégâts entre deux bornes kilométriques d'un chemin
     * 
     * @param cheminId l'ID du chemin
     * @param kmDebut  la borne kilométrique de début
     * @param kmFin    la borne kilométrique de fin
     * @return la liste des dégâts situés entre kmDebut et kmFin (inclus)
     */
    public List<Degat> getDegatsBetweenBornes(int cheminId, double kmDebut, double kmFin) throws SQLException {
        List<Degat> degats = new ArrayList<>();
        String sql = "SELECT * FROM DEGAT WHERE chemin_id = ? AND point_km >= ? AND point_km <= ? ORDER BY point_km";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cheminId);
            stmt.setDouble(2, kmDebut);
            stmt.setDouble(3, kmFin);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Degat degat = new Degat();
                    degat.setId(rs.getInt("id"));
                    degat.setCheminId(rs.getInt("chemin_id"));
                    degat.setPointKm(rs.getDouble("point_km"));
                    degat.setSurfaceM2(rs.getDouble("surface_m2"));
                    degat.setProfondeurM(rs.getDouble("profondeur_m"));

                    degats.add(degat);
                }
            }
        }
        return degats;
    }
}
