package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.ReparationDegat;
import model.Degat;
import model.Materiau;
import inc.OracleConnection;

public class ReparationDegatDAO {

    /**
     * Récupère toutes les réparations de dégâts
     */
    public List<ReparationDegat> getAll() throws SQLException {
        List<ReparationDegat> reparations = new ArrayList<>();
        String sql = """
                SELECT rd.id, rd.degat_id, rd.materiau_id, rd.validee, rd.cout_reparation,
                       ch.nom || ' - Point ' || d.point_km || 'km' AS degat_info,
                       m.nom AS materiau_nom,
                       d.surface_m2, d.profondeur_m
                FROM REPARATION_DEGAT rd
                INNER JOIN DEGAT d ON rd.degat_id = d.id
                INNER JOIN CHEMIN ch ON d.chemin_id = ch.id
                INNER JOIN MATERIAU m ON rd.materiau_id = m.id
                ORDER BY rd.id
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ReparationDegat rd = new ReparationDegat();
                rd.setId(rs.getInt("id"));
                rd.setDegatId(rs.getInt("degat_id"));
                rd.setMateriauId(rs.getInt("materiau_id"));
                rd.setValidee(rs.getInt("validee") == 1);

                double cout = rs.getDouble("cout_reparation");
                if (!rs.wasNull()) {
                    rd.setCoutReparation(cout);
                }

                rd.setDegatInfo(rs.getString("degat_info"));
                rd.setMateriauNom(rs.getString("materiau_nom"));
                rd.setSurfaceM2(rs.getDouble("surface_m2"));
                rd.setProfondeurM(rs.getDouble("profondeur_m"));

                reparations.add(rd);
            }
        }
        return reparations;
    }

    /**
     * Récupère les réparations validées uniquement
     */
    public List<ReparationDegat> getValidees() throws SQLException {
        List<ReparationDegat> reparations = new ArrayList<>();
        String sql = """
                SELECT rd.id, rd.degat_id, rd.materiau_id, rd.validee, rd.cout_reparation,
                       ch.nom || ' - Point ' || d.point_km || 'km' AS degat_info,
                       m.nom AS materiau_nom,
                       d.surface_m2, d.profondeur_m
                FROM REPARATION_DEGAT rd
                INNER JOIN DEGAT d ON rd.degat_id = d.id
                INNER JOIN CHEMIN ch ON d.chemin_id = ch.id
                INNER JOIN MATERIAU m ON rd.materiau_id = m.id
                WHERE rd.validee = 1
                ORDER BY rd.id
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ReparationDegat rd = new ReparationDegat();
                rd.setId(rs.getInt("id"));
                rd.setDegatId(rs.getInt("degat_id"));
                rd.setMateriauId(rs.getInt("materiau_id"));
                rd.setValidee(true);
                rd.setCoutReparation(rs.getDouble("cout_reparation"));
                rd.setDegatInfo(rs.getString("degat_info"));
                rd.setMateriauNom(rs.getString("materiau_nom"));
                rd.setSurfaceM2(rs.getDouble("surface_m2"));
                rd.setProfondeurM(rs.getDouble("profondeur_m"));

                reparations.add(rd);
            }
        }
        return reparations;
    }

    /**
     * Insère une nouvelle réparation de dégât
     */
    public int inserer(ReparationDegat reparationDegat) throws SQLException {
        if (reparationDegat.getDegatId() == null) {
            throw new SQLException("Le dégât est obligatoire");
        }
        if (reparationDegat.getMateriauId() == null) {
            throw new SQLException("Le matériau est obligatoire");
        }

        // Calculer le coût automatiquement
        double cout = calculerCoutReparation(reparationDegat.getDegatId(), reparationDegat.getMateriauId());

        String sql = """
                INSERT INTO REPARATION_DEGAT (id, degat_id, materiau_id, validee, cout_reparation)
                VALUES (SEQ_REPARATION_DEGAT.NEXTVAL, ?, ?, ?, ?)
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "id" })) {

            stmt.setInt(1, reparationDegat.getDegatId());
            stmt.setInt(2, reparationDegat.getMateriauId());
            stmt.setInt(3, reparationDegat.isValidee() ? 1 : 0);
            stmt.setDouble(4, cout);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Erreur lors de l'insertion");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Impossible de récupérer l'ID généré");
                }
            }
        }
    }

    /**
     * Calcule le coût de réparation pour un dégât et un matériau donnés
     */
    public double calculerCoutReparation(int degatId, int materiauId) throws SQLException {
        String sql = """
                SELECT d.surface_m2 * r.prix_par_m2 AS cout
                FROM DEGAT d, REPARATION r
                WHERE d.id = ?
                AND r.materiau_id = ?
                AND d.profondeur_m > r.profondeur_min
                AND d.profondeur_m <= r.profondeur_max
                """;

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, degatId);
            stmt.setInt(2, materiauId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("cout");
                } else {
                    throw new SQLException("Aucun tarif de réparation trouvé pour cette profondeur et ce matériau");
                }
            }
        }
    }

    /**
     * Valide une réparation de dégât
     */
    public void valider(int reparationDegatId) throws SQLException {
        String sql = "UPDATE REPARATION_DEGAT SET validee = 1 WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reparationDegatId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Réparation de dégât non trouvée");
            }
        }
    }

    /**
     * Annule la validation d'une réparation de dégât
     */
    public void annulerValidation(int reparationDegatId) throws SQLException {
        String sql = "UPDATE REPARATION_DEGAT SET validee = 0 WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reparationDegatId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Réparation de dégât non trouvée");
            }
        }
    }

    /**
     * Supprime une réparation de dégât
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM REPARATION_DEGAT WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Réparation de dégât non trouvée");
            }
        }
    }

    /**
     * Calcule le coût total de toutes les réparations validées
     */
    public double calculerCoutTotalValidees() throws SQLException {
        String sql = "SELECT COALESCE(SUM(cout_reparation), 0) FROM REPARATION_DEGAT WHERE validee = 1";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getDouble(1);
            }
            return 0.0;
        }
    }

    /**
     * Récupère tous les dégâts non encore affectés à une réparation
     */
    public List<Degat> getDegatsNonAffectes() throws SQLException {
        List<Degat> degats = new ArrayList<>();
        String sql = """
                SELECT d.id, d.chemin_id, d.point_km, d.surface_m2, d.profondeur_m
                FROM DEGAT d
                WHERE d.id NOT IN (SELECT degat_id FROM REPARATION_DEGAT)
                ORDER BY d.chemin_id, d.point_km
                """;

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
}