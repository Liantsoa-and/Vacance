package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import model.ResultatTrajet;
import model.Chemin;
import model.Dommage;
import model.Degat;
import controller.TrajetController;
import dao.DommageDAO;
import dao.DegatDAO;
import dao.ReparationDAO;

public class InfosEtResult extends JPanel {
    private JLabel lblVehicule;
    private JLabel lblVitesseMax;
    private JLabel lblLargeur;
    private JLabel lblReservoir;
    private JLabel lblConsommation;
    private JLabel lblAutonomie;
    private JTable tableItineraire;
    private JLabel lblDistanceTotal;
    private JLabel lblTempsTotal;
    private JLabel lblConsommationTrajet;
    private JLabel lblConformiteCarburant;
    private JLabel lblVitesseMoyenneReelle;
    private JLabel lblCoutReparationTotal;

    private DefaultTableModel tableModel;
    private ResultatTrajet resultatSelectionne;

    public InfosEtResult() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Informations et Résultats"));

        // Panel informations véhicule
        JPanel panelInfoVehicule = new JPanel(new GridLayout(6, 2, 10, 10));
        panelInfoVehicule.setBorder(BorderFactory.createTitledBorder("Véhicule Sélectionné"));

        panelInfoVehicule.add(new JLabel("Nom :"));
        lblVehicule = new JLabel("-");
        panelInfoVehicule.add(lblVehicule);

        panelInfoVehicule.add(new JLabel("Vitesse Max :"));
        lblVitesseMax = new JLabel("-");
        panelInfoVehicule.add(lblVitesseMax);

        panelInfoVehicule.add(new JLabel("Largeur :"));
        lblLargeur = new JLabel("-");
        panelInfoVehicule.add(lblLargeur);

        panelInfoVehicule.add(new JLabel("Réservoir :"));
        lblReservoir = new JLabel("-");
        panelInfoVehicule.add(lblReservoir);

        panelInfoVehicule.add(new JLabel("Consommation :"));
        lblConsommation = new JLabel("-");
        panelInfoVehicule.add(lblConsommation);

        panelInfoVehicule.add(new JLabel("Autonomie Max :"));
        lblAutonomie = new JLabel("-");
        panelInfoVehicule.add(lblAutonomie);

        // Panel itinéraire
        JPanel panelItineraire = new JPanel(new BorderLayout());
        panelItineraire.setBorder(BorderFactory.createTitledBorder("Itinéraire"));

        tableModel = new DefaultTableModel(
                new String[] { "Chemin", "Distance (km)", "Vitesse Moyenne Réelle (km/h)", "Dégâts",
                        "Coût Réparation (Ar)" },
                0);
        tableItineraire = new JTable(tableModel);
        panelItineraire.add(new JScrollPane(tableItineraire), BorderLayout.CENTER);

        // Panel résumé
        JPanel panelResume = new JPanel(new GridLayout(6, 2, 10, 10));
        panelResume.setBorder(BorderFactory.createTitledBorder("Résumé du Trajet"));

        panelResume.add(new JLabel("Distance Totale :"));
        lblDistanceTotal = new JLabel("-");
        lblDistanceTotal.setFont(new Font("Arial", Font.BOLD, 12));
        panelResume.add(lblDistanceTotal);

        panelResume.add(new JLabel("Temps Total :"));
        lblTempsTotal = new JLabel("-");
        lblTempsTotal.setFont(new Font("Arial", Font.BOLD, 12));
        panelResume.add(lblTempsTotal);

        panelResume.add(new JLabel("Vitesse Moyenne Réelle :"));
        lblVitesseMoyenneReelle = new JLabel("-");
        lblVitesseMoyenneReelle.setFont(new Font("Arial", Font.BOLD, 12));
        panelResume.add(lblVitesseMoyenneReelle);

        panelResume.add(new JLabel("Carburant Nécessaire :"));
        lblConsommationTrajet = new JLabel("-");
        lblConsommationTrajet.setFont(new Font("Arial", Font.BOLD, 12));
        panelResume.add(lblConsommationTrajet);

        panelResume.add(new JLabel("Conformité Carburant :"));
        lblConformiteCarburant = new JLabel("-");
        lblConformiteCarburant.setFont(new Font("Arial", Font.BOLD, 12));
        panelResume.add(lblConformiteCarburant);

        panelResume.add(new JLabel("Coût Réparation Total :"));
        lblCoutReparationTotal = new JLabel("-");
        lblCoutReparationTotal.setFont(new Font("Arial", Font.BOLD, 12));
        lblCoutReparationTotal.setForeground(new Color(0, 100, 150));
        panelResume.add(lblCoutReparationTotal);

        // Assemblage
        JPanel panelHaut = new JPanel(new BorderLayout(10, 10));
        panelHaut.add(panelInfoVehicule, BorderLayout.WEST);
        panelHaut.add(panelItineraire, BorderLayout.CENTER);

        add(panelHaut, BorderLayout.CENTER);
        add(panelResume, BorderLayout.SOUTH);
    }

    public void afficherResultat(ResultatTrajet resultat) {
        this.resultatSelectionne = resultat;

        // Informations véhicule
        if (resultat.getVehicule() != null) {
            lblVehicule.setText(resultat.getVehicule().getNom());
            lblVitesseMax.setText(String.format("%.2f km/h", resultat.getVehicule().getVitesseMax()));
            lblLargeur.setText(String.format("%.2f m", resultat.getVehicule().getLargeur()));
            lblReservoir.setText(String.format("%.2f L", resultat.getVehicule().getReservoirL()));
            lblConsommation.setText(String.format("%.2f L/100km", resultat.getVehicule().getConsommationL100km()));
            lblAutonomie.setText(String.format("%.2f km", resultat.getVehicule().calculerAutonomie()));
        }

        // Itinéraire
        tableModel.setRowCount(0);
        List<Chemin> itineraire = resultat.getItineraire();
        DommageDAO daoDommage = new DommageDAO();
        DegatDAO daoDegatDAO = new DegatDAO();
        ReparationDAO reparationDAO = new ReparationDAO();

        for (Chemin c : itineraire) {
            try {
                List<Dommage> dommages = daoDommage.getByCheminId(c.getId());
                List<Degat> degats = daoDegatDAO.getParChemin(c.getId());

                // Calculer la vitesse moyenne réelle pour ce chemin
                double vitesseReelleChemin = TrajetController.calculerVitesseMoyenneReelleChemin(c, dommages, 100.0);

                // Compter les dégâts
                String infoDegats = degats.size() + " dégât(s)";

                // Calculer le coût de réparation pour ce chemin
                List<Integer> cheminIds = new java.util.ArrayList<>();
                cheminIds.add(c.getId());
                double coutChemin = reparationDAO.calculerCoutPourChemins(cheminIds);
                String coutFormate = String.format("%.2f", coutChemin);

                tableModel.addRow(new Object[] {
                        c.getNom(),
                        String.format("%.2f", c.getDistance()),
                        String.format("%.2f", vitesseReelleChemin),
                        infoDegats,
                        coutFormate
                });
            } catch (SQLException e) {
                // En cas d'erreur, afficher quand même le chemin avec valeurs par défaut
                tableModel.addRow(new Object[] {
                        c.getNom(),
                        String.format("%.2f", c.getDistance()),
                        "N/A",
                        "N/A",
                        "N/A"
                });
            }
        }

        // Résumé
        lblDistanceTotal.setText(String.format("%.2f km", resultat.getDistanceTotaleKm()));
        lblTempsTotal.setText(TrajetController.formatDuree(resultat.getTempsTotalHeures()));
        lblVitesseMoyenneReelle.setText(String.format("%.2f km/h", resultat.getVitesseMoyenneReelle()));

        // Afficher le coût total de réparation
        lblCoutReparationTotal.setText(String.format("%.2f Ar", resultat.getCoutReparationAr()));

        // Afficher le besoin en carburant
        double consommationCarburant = resultat.calculerConsommationCarburant();
        lblConsommationTrajet.setText(String.format("%.2f L", consommationCarburant));

        // Afficher la conformité carburant
        boolean peutFaire = resultat.peutFaireTrajetAvecPleinReservoir();
        if (peutFaire) {
            lblConformiteCarburant.setText("✓ OK (Réservoir suffisant)");
            lblConformiteCarburant.setForeground(new Color(0, 150, 0));
        } else {
            double deficit = consommationCarburant - resultat.getVehicule().getReservoirL();
            lblConformiteCarburant.setText("✗ IMPOSSIBLE (Déficit: " + String.format("%.2f", deficit) + " L)");
            lblConformiteCarburant.setForeground(Color.RED);
        }
    }

    public ResultatTrajet getResultatSelectionne() {
        return resultatSelectionne;
    }

    public void ajouterEcoutantBouton(javax.swing.event.ChangeListener listener) {
        // Placeholder pour éviter les erreurs
    }
}
