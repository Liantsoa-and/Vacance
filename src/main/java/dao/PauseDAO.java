package dao;

import inc.OracleConnection;
import model.Pause;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class PauseDAO {
    public List<Pause> getAll() {
        List<Pause> listePauses = new ArrayList<>();

        String sql = "SELECT * FROM PAUSE ORDER BY id";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int cheminId = rs.getInt("chemin_id");
                double pointKm = rs.getDouble("point_km");
                Timestamp heureDebut = rs.getTimestamp("heure_debut");
                Timestamp heureFin = rs.getTimestamp("heure_fin");

                Pause pause = new Pause(id, cheminId, pointKm, new java.util.Date(heureDebut.getTime()),
                        new java.util.Date(heureFin.getTime()));
                listePauses.add(pause);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans PauseDAO.getAll() : " + e.getMessage());
            e.printStackTrace();
        }

        return listePauses;
    }

    public Pause getById(int idCherche) {
        Pause pause = null;
        String sql = "SELECT id, chemin_id, point_km, heure_debut, heure_fin FROM PAUSE WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCherche);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Timestamp heureDebut = rs.getTimestamp("heure_debut");
                    Timestamp heureFin = rs.getTimestamp("heure_fin");
                    pause = new Pause(rs.getInt("id"), rs.getInt("chemin_id"), rs.getDouble("point_km"),
                            new java.util.Date(heureDebut.getTime()),
                            new java.util.Date(heureFin.getTime()));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return pause;
    }

    public List<Pause> getByCheminId(int cheminId) {
        List<Pause> listePauses = new ArrayList<>();
        String sql = "SELECT * FROM PAUSE WHERE chemin_id = ? ORDER BY heure_debut";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, cheminId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    double pointKm = rs.getDouble("point_km");
                    Timestamp heureDebut = rs.getTimestamp("heure_debut");
                    Timestamp heureFin = rs.getTimestamp("heure_fin");

                    Pause pause = new Pause(id, cheminId, pointKm,
                            new java.util.Date(heureDebut.getTime()),
                            new java.util.Date(heureFin.getTime()));
                    listePauses.add(pause);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return listePauses;
    }

    public void insert(Pause pause) {
        String sql = "INSERT INTO PAUSE (id, chemin_id, point_km, heure_debut, heure_fin) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pause.getId());
            stmt.setInt(2, pause.getCheminId());
            stmt.setDouble(3, pause.getPointKm());
            stmt.setTimestamp(4, new Timestamp(pause.getHeureDebut().getTime()));
            stmt.setTimestamp(5, new Timestamp(pause.getHeureFin().getTime()));
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans PauseDAO.insert() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void update(Pause pause) {
        String sql = "UPDATE PAUSE SET chemin_id = ?, point_km = ?, heure_debut = ?, heure_fin = ? WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pause.getCheminId());
            stmt.setDouble(2, pause.getPointKm());
            stmt.setTimestamp(3, new Timestamp(pause.getHeureDebut().getTime()));
            stmt.setTimestamp(4, new Timestamp(pause.getHeureFin().getTime()));
            stmt.setInt(5, pause.getId());
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans PauseDAO.update() : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM PAUSE WHERE id = ?";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans PauseDAO.delete() : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
