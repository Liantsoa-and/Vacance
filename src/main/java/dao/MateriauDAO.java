package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Materiau;
import inc.OracleConnection;

public class MateriauDAO {

    /**
     * Récupère tous les matériaux
     */
    public List<Materiau> getAll() throws SQLException {
        List<Materiau> materiaux = new ArrayList<>();
        String sql = "SELECT * FROM MATERIAU ORDER BY nom";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Materiau materiau = new Materiau();
                materiau.setId(rs.getInt("id"));
                materiau.setNom(rs.getString("nom"));
                materiau.setDescription(rs.getString("description"));
                materiaux.add(materiau);
            }
        }
        return materiaux;
    }

    /**
     * Récupère un matériau par ID
     */
    public Materiau getById(int id) throws SQLException {
        String sql = "SELECT * FROM MATERIAU WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Materiau materiau = new Materiau();
                    materiau.setId(rs.getInt("id"));
                    materiau.setNom(rs.getString("nom"));
                    materiau.setDescription(rs.getString("description"));
                    return materiau;
                }
            }
        }
        return null;
    }

    /**
     * Vérifie si un matériau avec ce nom existe déjà
     */
    public boolean existeParNom(String nom) throws SQLException {
        String sql = "SELECT COUNT(*) FROM MATERIAU WHERE UPPER(nom) = UPPER(?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nom);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Insère un nouveau matériau
     * 
     * @return l'ID généré
     */
    public int inserer(Materiau materiau) throws SQLException {
        // Vérification: nom non vide
        if (materiau.getNom() == null || materiau.getNom().trim().isEmpty()) {
            throw new SQLException("Le nom du matériau est obligatoire");
        }

        // Vérification: nom unique
        if (existeParNom(materiau.getNom())) {
            throw new SQLException("Un matériau avec ce nom existe déjà");
        }

        String sql = "INSERT INTO MATERIAU (id, nom, description) VALUES (SEQ_MATERIAU.NEXTVAL, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "id" })) {

            stmt.setString(1, materiau.getNom().trim());
            stmt.setString(2, materiau.getDescription());

            int affectes = stmt.executeUpdate();

            if (affectes > 0) {
                // Récupération de l'ID généré
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Échec de l'insertion du matériau");
    }

    /**
     * Supprime un matériau (si pas utilisé dans REPARATION ou DEGAT)
     */
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM MATERIAU WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectes = stmt.executeUpdate();

            if (affectes == 0) {
                throw new SQLException("Matériau introuvable");
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 2291 || e.getMessage().contains("integrity constraint")) {
                throw new SQLException(
                        "Impossible de supprimer: le matériau est utilisé dans des réparations ou dégâts");
            }
            throw e;
        }
    }
}
