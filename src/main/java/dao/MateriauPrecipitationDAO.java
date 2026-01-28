package dao;

import model.MateriauPrecipitation;
import inc.OracleConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MateriauPrecipitationDAO {
    private Connection connection;

    public MateriauPrecipitationDAO() {
        this.connection = OracleConnection.getConnection();
    }

    /**
     * Récupère toutes les configurations matériau-précipitation
     */
    public List<MateriauPrecipitation> findAll() throws SQLException {
        List<MateriauPrecipitation> configurations = new ArrayList<>();
        String sql = "SELECT id, materiau_id, niveau_min_mm, niveau_max_mm FROM MATERIAU_PRECIPITATION ORDER BY niveau_min_mm";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MateriauPrecipitation mp = new MateriauPrecipitation(
                        rs.getInt("id"),
                        rs.getInt("materiau_id"),
                        rs.getDouble("niveau_min_mm"),
                        rs.getDouble("niveau_max_mm"));
                configurations.add(mp);
            }
        }

        return configurations;
    }

    /**
     * Trouve le matériau correspondant à un niveau de précipitation
     * Logique : niveau_min_mm < niveau <= niveau_max_mm
     */
    public MateriauPrecipitation findByPrecipitationLevel(double niveau) throws SQLException {
        String sql = "SELECT id, materiau_id, niveau_min_mm, niveau_max_mm FROM MATERIAU_PRECIPITATION " +
                "WHERE niveau_min_mm < ? AND niveau_max_mm >= ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDouble(1, niveau);
            stmt.setDouble(2, niveau);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MateriauPrecipitation(
                            rs.getInt("id"),
                            rs.getInt("materiau_id"),
                            rs.getDouble("niveau_min_mm"),
                            rs.getDouble("niveau_max_mm"));
                }
            }
        }

        return null; // Aucun matériau trouvé pour ce niveau
    }

    /**
     * Ajoute une nouvelle configuration matériau-précipitation
     */
    public boolean create(MateriauPrecipitation mp) throws SQLException {
        // Vérifier d'abord que les intervalles restent cohérents
        if (!validateIntervals(mp)) {
            throw new SQLException("Configuration invalide : chevauchement ou gap dans les intervalles");
        }

        String sql = "INSERT INTO MATERIAU_PRECIPITATION (id, materiau_id, niveau_min_mm, niveau_max_mm) VALUES (SEQ_MATERIAU_PRECIPITATION.NEXTVAL, ?, ?, ?)";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mp.getMateriauId());
            stmt.setDouble(2, mp.getNiveauMin());
            stmt.setDouble(3, mp.getNiveauMax());

            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Modifie une configuration matériau-précipitation existante
     */
    public boolean update(MateriauPrecipitation mp) throws SQLException {
        // Vérifier que les intervalles restent cohérents
        if (!validateIntervals(mp)) {
            throw new SQLException("Configuration invalide : chevauchement ou gap dans les intervalles");
        }

        String sql = "UPDATE MATERIAU_PRECIPITATION SET materiau_id = ?, niveau_min_mm = ?, niveau_max_mm = ? WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, mp.getMateriauId());
            stmt.setDouble(2, mp.getNiveauMin());
            stmt.setDouble(3, mp.getNiveauMax());
            stmt.setInt(4, mp.getId());

            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Supprime une configuration matériau-précipitation
     */
    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM MATERIAU_PRECIPITATION WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            boolean result = stmt.executeUpdate() > 0;
            connection.commit();
            return result;
        }
    }

    /**
     * Vérifie que les intervalles se couvrent bien et ne se chevauchent pas
     */
    public boolean validateIntervals() throws SQLException {
        String sql = "SELECT niveau_min_mm, niveau_max_mm FROM MATERIAU_PRECIPITATION ORDER BY niveau_min_mm";

        try (PreparedStatement stmt = connection.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            double dernierMax = 0.0; // On commence à 0
            boolean first = true;

            while (rs.next()) {
                double min = rs.getDouble("niveau_min_mm");
                double max = rs.getDouble("niveau_max_mm");

                // Vérifier que l'intervalle est valide
                if (min >= max) {
                    return false; // Intervalle invalide
                }

                if (!first) {
                    // Vérifier qu'il n'y a pas de gap ou de chevauchement
                    if (Math.abs(min - dernierMax) > 0.001) { // Tolérance pour les erreurs d'arrondi
                        return false; // Gap ou chevauchement détecté
                    }
                }

                dernierMax = max;
                first = false;
            }
        }

        return true;
    }

    /**
     * Vérifie qu'une nouvelle configuration ne cassera pas la cohérence des
     * intervalles
     */
    private boolean validateIntervals(MateriauPrecipitation newMp) throws SQLException {
        // Accepter 0.0 comme niveau minimum valide
        if (newMp.getNiveauMin() < 0) {
            return false; // Seul négatif est invalide
        }

        if (newMp.getNiveauMin() >= newMp.getNiveauMax()) {
            return false; // Min doit être strictement inférieur à Max
        }

        List<MateriauPrecipitation> allConfigs = findAll();

        // Remplacer l'ancienne configuration si c'est un update
        allConfigs.removeIf(mp -> mp.getId() == newMp.getId());

        // Ajouter la nouvelle
        allConfigs.add(newMp);

        // Trier par niveau_min_mm
        allConfigs.sort((a, b) -> Double.compare(a.getNiveauMin(), b.getNiveauMin()));

        // Vérifier qu'il n'y a pas de chevauchement
        for (int i = 0; i < allConfigs.size() - 1; i++) {
            MateriauPrecipitation current = allConfigs.get(i);
            MateriauPrecipitation next = allConfigs.get(i + 1);

            // Vérifier le chevauchement : current.max doit être <= next.min
            if (current.getNiveauMax() > next.getNiveauMin() + 0.001) {
                return false; // Chevauchement détecté
            }
        }

        return true;
    }

    /**
     * Récupère une configuration par son ID
     */
    public MateriauPrecipitation findById(int id) throws SQLException {
        String sql = "SELECT id, materiau_id, niveau_min_mm, niveau_max_mm FROM MATERIAU_PRECIPITATION WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new MateriauPrecipitation(
                            rs.getInt("id"),
                            rs.getInt("materiau_id"),
                            rs.getDouble("niveau_min_mm"),
                            rs.getDouble("niveau_max_mm"));
                }
            }
        }

        return null;
    }

    /**
     * Récupère toutes les configurations pour un matériau donné
     */
    public List<MateriauPrecipitation> findByMateriauId(int materiauId) throws SQLException {
        List<MateriauPrecipitation> configurations = new ArrayList<>();
        String sql = "SELECT id, materiau_id, niveau_min_mm, niveau_max_mm FROM MATERIAU_PRECIPITATION " +
                "WHERE materiau_id = ? ORDER BY niveau_min_mm";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, materiauId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MateriauPrecipitation mp = new MateriauPrecipitation(
                            rs.getInt("id"),
                            rs.getInt("materiau_id"),
                            rs.getDouble("niveau_min_mm"),
                            rs.getDouble("niveau_max_mm"));
                    configurations.add(mp);
                }
            }
        }

        return configurations;
    }
}