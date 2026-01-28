package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Borne;
import inc.OracleConnection;

public class BorneDAO {

    /**
     * Récupère toutes les bornes
     */
    public List<Borne> getAll() throws SQLException {
        List<Borne> bornes = new ArrayList<>();
        String sql = "SELECT * FROM BORNE ORDER BY chemin_id, km";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Borne borne = new Borne(
                        rs.getInt("id"),
                        rs.getInt("chemin_id"),
                        rs.getDouble("km"));
                bornes.add(borne);
            }
        }
        return bornes;
    }

    /**
     * Récupère toutes les bornes d'un chemin
     */
    public List<Borne> getBornesByCheminId(int cheminId) throws SQLException {
        List<Borne> bornes = new ArrayList<>();
        String sql = "SELECT * FROM BORNE WHERE chemin_id = ? ORDER BY km";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cheminId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Borne borne = new Borne(
                            rs.getInt("id"),
                            rs.getInt("chemin_id"),
                            rs.getDouble("km"));
                    bornes.add(borne);
                }
            }
        }
        return bornes;
    }

    /**
     * Récupère une borne par son ID
     */
    public Borne getById(int id) throws SQLException {
        Borne borne = null;
        String sql = "SELECT * FROM BORNE WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    borne = new Borne(
                            rs.getInt("id"),
                            rs.getInt("chemin_id"),
                            rs.getDouble("km"));
                }
            }
        }
        return borne;
    }

    /**
     * Insère une nouvelle borne
     */
    public void insert(Borne borne) throws SQLException {
        String sql = "INSERT INTO BORNE (id, chemin_id, km) VALUES (SEQ_BORNE.NEXTVAL, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borne.getCheminId());
            stmt.setDouble(2, borne.getKm());
            stmt.executeUpdate();
        }
    }

    /**
     * Met à jour une borne
     */
    public void update(Borne borne) throws SQLException {
        String sql = "UPDATE BORNE SET chemin_id = ?, km = ? WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, borne.getCheminId());
            stmt.setDouble(2, borne.getKm());
            stmt.setInt(3, borne.getId());
            stmt.executeUpdate();
        }
    }

    /**
     * Supprime une borne
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM BORNE WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
}
