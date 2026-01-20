package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Reparation;
import inc.OracleConnection;

public class ReparationDAO {

    /**
     * Récupère toutes les réparations avec le nom du matériau
     */
    public List<Reparation> getAll() throws SQLException {
        List<Reparation> reparations = new ArrayList<>();
        String sql = "SELECT r.*, m.nom as materiau_nom " +
                "FROM REPARATION r " +
                "INNER JOIN MATERIAU m ON r.materiau_id = m.id " +
                "ORDER BY m.nom, r.profondeur_min";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Reparation reparation = new Reparation();
                reparation.setId(rs.getInt("id"));
                reparation.setMateriauId(rs.getInt("materiau_id"));
                reparation.setMateriauNom(rs.getString("materiau_nom"));
                reparation.setProfondeurMin(rs.getDouble("profondeur_min"));
                reparation.setProfondeurMax(rs.getDouble("profondeur_max"));
                reparation.setPrixParM2(rs.getDouble("prix_par_m2"));
                reparation.setDescription(rs.getString("description"));
                reparations.add(reparation);
            }
        }
        return reparations;
    }

    /**
     * Récupère les réparations pour un matériau spécifique
     */
    public List<Reparation> getParMateriau(int materiauId) throws SQLException {
        List<Reparation> reparations = new ArrayList<>();
        String sql = "SELECT r.*, m.nom as materiau_nom " +
                "FROM REPARATION r " +
                "INNER JOIN MATERIAU m ON r.materiau_id = m.id " +
                "WHERE r.materiau_id = ? " +
                "ORDER BY r.profondeur_min";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, materiauId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Reparation reparation = new Reparation();
                    reparation.setId(rs.getInt("id"));
                    reparation.setMateriauId(rs.getInt("materiau_id"));
                    reparation.setMateriauNom(rs.getString("materiau_nom"));
                    reparation.setProfondeurMin(rs.getDouble("profondeur_min"));
                    reparation.setProfondeurMax(rs.getDouble("profondeur_max"));
                    reparation.setPrixParM2(rs.getDouble("prix_par_m2"));
                    reparation.setDescription(rs.getString("description"));
                    reparations.add(reparation);
                }
            }
        }
        return reparations;
    }

    /**
     * Vérifie si un intervalle chevauche un intervalle existant pour un matériau
     * Chevauchement si: (new_min < existing_max) AND (new_max > existing_min)
     */
    public boolean intervalleChevauche(int materiauId, double min, double max, Integer idExclu) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REPARATION " +
                "WHERE materiau_id = ? " +
                "AND profondeur_min < ? " +
                "AND profondeur_max > ? " +
                (idExclu != null ? "AND id != ? " : "");

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, materiauId);
            stmt.setDouble(2, max);
            stmt.setDouble(3, min);
            if (idExclu != null) {
                stmt.setInt(4, idExclu);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Vérifie si une réparation identique existe déjà
     */
    public boolean existeDeja(int materiauId, double min, double max, Integer idExclu) throws SQLException {
        String sql = "SELECT COUNT(*) FROM REPARATION " +
                "WHERE materiau_id = ? " +
                "AND profondeur_min = ? " +
                "AND profondeur_max = ? " +
                (idExclu != null ? "AND id != ? " : "");

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, materiauId);
            stmt.setDouble(2, min);
            stmt.setDouble(3, max);
            if (idExclu != null) {
                stmt.setInt(4, idExclu);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Insère une nouvelle réparation avec toutes les validations
     * 
     * @return l'ID généré
     */
    public int inserer(Reparation reparation) throws SQLException {
        // Validations de base
        if (reparation.getMateriauId() == null) {
            throw new SQLException("Le matériau est obligatoire");
        }
        if (reparation.getProfondeurMin() < 0) {
            throw new SQLException("La profondeur minimale doit être >= 0");
        }
        if (reparation.getProfondeurMax() <= reparation.getProfondeurMin()) {
            throw new SQLException("La profondeur maximale doit être > profondeur minimale");
        }
        if (reparation.getPrixParM2() <= 0) {
            throw new SQLException("Le prix par m² doit être > 0");
        }

        // Vérification: déduplication exacte
        if (existeDeja(reparation.getMateriauId(), reparation.getProfondeurMin(),
                reparation.getProfondeurMax(), null)) {
            throw new SQLException("Cette combinaison matériau/intervalle existe déjà");
        }

        // Vérification: anti-chevauchement
        if (intervalleChevauche(reparation.getMateriauId(), reparation.getProfondeurMin(),
                reparation.getProfondeurMax(), null)) {
            throw new SQLException("Cet intervalle chevauche un intervalle existant pour ce matériau");
        }

        String sql = "INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) "
                +
                "VALUES (SEQ_REPARATION.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "id" })) {

            stmt.setInt(1, reparation.getMateriauId());
            stmt.setDouble(2, reparation.getProfondeurMin());
            stmt.setDouble(3, reparation.getProfondeurMax());
            stmt.setDouble(4, reparation.getPrixParM2());
            stmt.setString(5, reparation.getDescription());

            int affectes = stmt.executeUpdate();

            if (affectes > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Échec de l'insertion de la réparation");
    }

    /**
     * Supprime une réparation
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM REPARATION WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectes = stmt.executeUpdate();

            if (affectes == 0) {
                throw new SQLException("Réparation introuvable");
            }
        }
    }

    /**
     * Calcule le coût total de réparation pour un ou plusieurs chemins
     */
    public double calculerCoutPourChemins(List<Integer> cheminIds) throws SQLException {
        if (cheminIds == null || cheminIds.isEmpty()) {
            return 0.0;
        }

        // Construction de la clause IN
        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < cheminIds.size(); i++) {
            if (i > 0)
                placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT SUM(cout_reparation) as cout_total " +
                "FROM VUE_DEGAT_COUT " +
                "WHERE chemin_id IN (" + placeholders + ")";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < cheminIds.size(); i++) {
                stmt.setInt(i + 1, cheminIds.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    double cout = rs.getDouble("cout_total");
                    return rs.wasNull() ? 0.0 : cout;
                }
            }
        }
        return 0.0;
    }
}
