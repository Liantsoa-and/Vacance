package controller;

import java.sql.SQLException;
import java.util.List;

import dao.DegatDAO;
import dao.ReparationDegatDAO;
import dao.BorneDAO;
import dao.ReparationDAO;
import model.Degat;
import model.Materiau;
import model.Reparation;

/**
 * Contrôleur pour gérer les opérations sur les bornes kilométriques
 * et calculer les coûts de réparation entre deux bornes
 */
public class BorneController {

    private DegatDAO degatDAO;
    private ReparationDegatDAO reparationDegatDAO;
    private BorneDAO borneDAO;
    private MaterialSelectionService materialService;
    private ReparationDAO reparationDAO;

    public BorneController() {
        this.degatDAO = new DegatDAO();
        this.reparationDegatDAO = new ReparationDegatDAO();
        this.borneDAO = new BorneDAO();
        this.materialService = new MaterialSelectionService();
        this.reparationDAO = new ReparationDAO();
    }

    /**
     * Récupère tous les dégâts d'un chemin spécifique
     * 
     * @param cheminId l'ID du chemin
     * @return la liste de tous les dégâts du chemin
     * @throws SQLException en cas d'erreur de base de données
     */
    public List<Degat> getAllDegatsByCheminId(int cheminId) throws SQLException {
        return degatDAO.getDegatsByCheminId(cheminId);
    }

    /**
     * Récupère les dégâts entre deux bornes kilométriques d'un chemin
     * 
     * @param cheminId l'ID du chemin
     * @param kmDebut  la borne kilométrique de début
     * @param kmFin    la borne kilométrique de fin
     * @return la liste des dégâts situés entre les deux bornes
     * @throws SQLException en cas d'erreur de base de données
     */
    public List<Degat> getDegatsBetweenBornes(int cheminId, double kmDebut, double kmFin) throws SQLException {
        if (kmDebut > kmFin) {
            throw new IllegalArgumentException("La borne de début doit être inférieure à la borne de fin");
        }
        return degatDAO.getDegatsBetweenBornes(cheminId, kmDebut, kmFin);
    }

    /**
     * Calcule le prix total de réparation entre deux bornes kilométriques
     * Suit la logique des précipitations : sélection automatique du matériau
     * selon le niveau de précipitation à la position du dégât
     * 
     * @param cheminId l'ID du chemin
     * @param kmDebut  la borne kilométrique de début
     * @param kmFin    la borne kilométrique de fin
     * @return le coût total de réparation pour tous les dégâts entre les deux
     *         bornes
     * @throws SQLException en cas d'erreur de base de données
     */
    public double calculerPrixReparationBetweenBornes(int cheminId, double kmDebut, double kmFin) throws SQLException {
        // Récupérer les dégâts entre les deux bornes
        List<Degat> degats = getDegatsBetweenBornes(cheminId, kmDebut, kmFin);

        double coutTotal = 0.0;

        // Pour chaque dégât, sélectionner le matériau selon les précipitations et
        // calculer le coût
        for (Degat degat : degats) {
            try {
                // 1. Sélection automatique du matériau selon les précipitations
                Materiau materiau = materialService.getMaterialForDommage(degat.getCheminId(), degat.getPointKm());

                // 2. Trouver la réparation appropriée selon la profondeur
                Reparation reparation = reparationDAO.findByMateriauAndProfondeur(
                        materiau.getId(),
                        degat.getProfondeurM());

                if (reparation != null) {
                    // 3. Calculer le coût : surface × prix au m²
                    double cout = degat.getSurfaceM2() * reparation.getPrixParM2();
                    coutTotal += cout;
                }
            } catch (SQLException e) {
                System.err.println(
                        "Erreur lors du calcul pour le dégât au km " + degat.getPointKm() + " : " + e.getMessage());
                // Continue avec les autres dégâts même en cas d'erreur
            }
        }

        return coutTotal;
    }

    /**
     * Classe pour encapsuler les résultats du calcul de réparation entre deux
     * bornes
     */
    public static class ResultatReparationBornes {
        private double kmDebut;
        private double kmFin;
        private List<DegatAvecCout> degatsAvecCout;
        private double coutTotal;
        private int nombreDegats;

        public ResultatReparationBornes(double kmDebut, double kmFin, List<DegatAvecCout> degatsAvecCout,
                double coutTotal) {
            this.kmDebut = kmDebut;
            this.kmFin = kmFin;
            this.degatsAvecCout = degatsAvecCout;
            this.coutTotal = coutTotal;
            this.nombreDegats = degatsAvecCout != null ? degatsAvecCout.size() : 0;
        }

        // Getters
        public double getKmDebut() {
            return kmDebut;
        }

        public double getKmFin() {
            return kmFin;
        }

        public List<DegatAvecCout> getDegatsAvecCout() {
            return degatsAvecCout;
        }

        public double getCoutTotal() {
            return coutTotal;
        }

        public int getNombreDegats() {
            return nombreDegats;
        }

        @Override
        public String toString() {
            return String.format("Réparation entre %.2f km et %.2f km : %d dégâts, coût total = %.2f €",
                    kmDebut, kmFin, nombreDegats, coutTotal);
        }
    }

    /**
     * Classe pour encapsuler un dégât avec son coût de réparation calculé
     */
    public static class DegatAvecCout {
        private Degat degat;
        private Materiau materiau;
        private double coutReparation;
        private String descriptionReparation;

        public DegatAvecCout(Degat degat, Materiau materiau, double coutReparation, String descriptionReparation) {
            this.degat = degat;
            this.materiau = materiau;
            this.coutReparation = coutReparation;
            this.descriptionReparation = descriptionReparation;
        }

        public Degat getDegat() {
            return degat;
        }

        public Materiau getMateriau() {
            return materiau;
        }

        public double getCoutReparation() {
            return coutReparation;
        }

        public String getDescriptionReparation() {
            return descriptionReparation;
        }
    }

    /**
     * Calcule le prix de réparation et retourne un objet avec tous les détails
     * Suit la logique des précipitations pour la sélection automatique du matériau
     * 
     * @param cheminId l'ID du chemin
     * @param kmDebut  la borne kilométrique de début
     * @param kmFin    la borne kilométrique de fin
     * @return un objet ResultatReparationBornes contenant tous les détails
     * @throws SQLException en cas d'erreur de base de données
     */
    public ResultatReparationBornes calculerReparationComplete(int cheminId, double kmDebut, double kmFin)
            throws SQLException {
        // Récupérer les dégâts entre les deux bornes
        List<Degat> degats = getDegatsBetweenBornes(cheminId, kmDebut, kmFin);

        List<DegatAvecCout> degatsAvecCout = new java.util.ArrayList<>();
        double coutTotal = 0.0;

        // Pour chaque dégât, calculer le coût avec sélection automatique du matériau
        for (Degat degat : degats) {
            try {
                // 1. Sélection automatique du matériau selon les précipitations
                Materiau materiau = materialService.getMaterialForDommage(degat.getCheminId(), degat.getPointKm());

                // 2. Trouver la réparation appropriée selon la profondeur
                Reparation reparation = reparationDAO.findByMateriauAndProfondeur(
                        materiau.getId(),
                        degat.getProfondeurM());

                if (reparation != null) {
                    // 3. Calculer le coût : surface × prix au m²
                    double cout = degat.getSurfaceM2() * reparation.getPrixParM2();
                    coutTotal += cout;

                    // 4. Créer l'objet avec les détails
                    DegatAvecCout degatAvecCout = new DegatAvecCout(
                            degat,
                            materiau,
                            cout,
                            reparation.getDescription());
                    degatsAvecCout.add(degatAvecCout);
                } else {
                    // Aucune réparation trouvée pour cette profondeur
                    DegatAvecCout degatAvecCout = new DegatAvecCout(
                            degat,
                            materiau,
                            0.0,
                            "Aucune réparation disponible pour cette profondeur");
                    degatsAvecCout.add(degatAvecCout);
                }
            } catch (SQLException e) {
                System.err.println(
                        "Erreur lors du calcul pour le dégât au km " + degat.getPointKm() + " : " + e.getMessage());
                // Ajouter quand même le dégât avec un coût de 0
                DegatAvecCout degatAvecCout = new DegatAvecCout(
                        degat,
                        null,
                        0.0,
                        "Erreur: " + e.getMessage());
                degatsAvecCout.add(degatAvecCout);
            }
        }

        return new ResultatReparationBornes(kmDebut, kmFin, degatsAvecCout, coutTotal);
    }
}
