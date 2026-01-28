package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.ReparationDegat;
import model.Degat;
import model.Materiau;
import model.Reparation;
import inc.OracleConnection;
import controller.MaterialSelectionService;

public class ReparationDegatDAO {

    // Constantes SQL
    private static final String SQL_BASE_SELECT_REPARATIONS = """
            SELECT rd.id, rd.degat_id, rd.materiau_id, rd.validee, rd.cout_reparation,
                   ch.nom || ' - Point ' || d.point_km || 'km' AS degat_info,
                   m.nom AS materiau_nom,
                   d.surface_m2, d.profondeur_m
            FROM REPARATION_DEGAT rd
            INNER JOIN DEGAT d ON rd.degat_id = d.id
            INNER JOIN CHEMIN ch ON d.chemin_id = ch.id
            INNER JOIN MATERIAU m ON rd.materiau_id = m.id
            """;

    /**
     * Valide les paramètres obligatoires d'une réparation de dégât
     */
    private void validerParametresReparation(ReparationDegat reparationDegat) throws SQLException {
        if (reparationDegat.getDegatId() == null) {
            throw new SQLException("Le dégât est obligatoire");
        }
        if (reparationDegat.getMateriauId() == null) {
            throw new SQLException("Le matériau est obligatoire");
        }
    }

    /**
     * Mappe un ResultSet vers un objet ReparationDegat
     */
    private ReparationDegat mapperReparationDegat(ResultSet rs) throws SQLException {
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

        return rd;
    }

    /**
     * Exécute une requête de sélection de réparations avec une clause WHERE
     * optionnelle
     */
    private List<ReparationDegat> executerRequeteReparations(String whereClause) throws SQLException {
        List<ReparationDegat> reparations = new ArrayList<>();
        String sql = SQL_BASE_SELECT_REPARATIONS;

        if (whereClause != null && !whereClause.trim().isEmpty()) {
            sql += " WHERE " + whereClause;
        }

        sql += " ORDER BY rd.id";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                reparations.add(mapperReparationDegat(rs));
            }
        }
        return reparations;
    }

    /**
     * Exécute une mise à jour simple avec un paramètre ID
     */
    private void executerMiseAJourParId(String sql, int id, String messageErreur) throws SQLException {
        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException(messageErreur);
            }
        }
    }

    /**
     * Récupère toutes les réparations de dégâts
     */
    public List<ReparationDegat> getAll() throws SQLException {
        return executerRequeteReparations(null);
    }

    /**
     * Récupère les réparations validées uniquement
     */
    public List<ReparationDegat> getValidees() throws SQLException {
        return executerRequeteReparations("rd.validee = 1");
    }

    /**
     * Insère une nouvelle réparation de dégât
     */
    public int inserer(ReparationDegat reparationDegat) throws SQLException {
        validerParametresReparation(reparationDegat);

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
        executerMiseAJourParId(
                "UPDATE REPARATION_DEGAT SET validee = 1 WHERE id = ?",
                reparationDegatId,
                "Réparation de dégât non trouvée");
    }

    /**
     * Annule la validation d'une réparation de dégât
     */
    public void annulerValidation(int reparationDegatId) throws SQLException {
        executerMiseAJourParId(
                "UPDATE REPARATION_DEGAT SET validee = 0 WHERE id = ?",
                reparationDegatId,
                "Réparation de dégât non trouvée");
    }

    /**
     * Supprime une réparation de dégât
     */
    public void supprimer(int id) throws SQLException {
        executerMiseAJourParId(
                "DELETE FROM REPARATION_DEGAT WHERE id = ?",
                id,
                "Réparation de dégât non trouvée");
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

    /**
     * Insère une nouvelle réparation avec sélection automatique du matériau
     * basée sur les précipitations du chemin
     */
    public int insererAvecSelectionAutomatique(ReparationDegat reparationDegat) throws SQLException {
        if (reparationDegat.getDegatId() == null) {
            throw new SQLException("Le dégât est obligatoire");
        }

        MaterialSelectionService materialService = new MaterialSelectionService();
        DegatDAO degatDAO = new DegatDAO();

        // 1. Récupérer les infos du dégât
        Degat degat = degatDAO.getById(reparationDegat.getDegatId());
        if (degat == null) {
            throw new SQLException("Dégât introuvable avec l'ID: " + reparationDegat.getDegatId());
        }

        // 2. Sélection automatique du matériau selon les précipitations
        Materiau materiau = materialService.getMaterialForDommage(degat.getCheminId(), degat.getPointKm());
        
        // 3. Affecter le matériau sélectionné automatiquement
        reparationDegat.setMateriauId(materiau.getId());
        
        // 4. Log pour information
        System.out.println("Matériau sélectionné automatiquement: " + materiau.getNom() 
                        + " pour dégât au km " + degat.getPointKm());

        // 5. Utiliser la méthode d'insertion existante (qui calcule déjà le coût)
        return inserer(reparationDegat);
    }
}