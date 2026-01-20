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

                int materiauId = rs.getInt("materiau_id");
                if (!rs.wasNull()) {
                    degat.setMateriauId(materiauId);
                }

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

                    int materiauId = rs.getInt("materiau_id");
                    if (!rs.wasNull()) {
                        degat.setMateriauId(materiauId);
                    }

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

        String sql = "INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m, materiau_id) " +
                "VALUES (SEQ_DEGAT.NEXTVAL, ?, ?, ?, ?, ?)";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, new String[] { "id" })) {

            stmt.setInt(1, degat.getCheminId());
            stmt.setDouble(2, degat.getPointKm());
            stmt.setDouble(3, degat.getSurfaceM2());
            stmt.setDouble(4, degat.getProfondeurM());

            if (degat.getMateriauId() != null) {
                stmt.setInt(5, degat.getMateriauId());
            } else {
                stmt.setNull(5, Types.INTEGER);
            }

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
}
