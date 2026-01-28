package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.List;
import controller.TrajetController;
import model.ResultatTrajet;
import model.Vehicule;
import java.util.Date;

public class MainFrame extends JFrame {
    private SelectionPanel panelSelection;
    private ChoicePanel panelChoice;
    private InfosEtResult panelInfos;
    private NavigationPanel panelNavigation;

    private List<ResultatTrajet> derniersResultats;

    public MainFrame() {
        setTitle("Calculateur de Trajets avec Navigation");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(2000, 1500);
        setLocationRelativeTo(null);
        setResizable(true);

        // Créer les panels
        panelSelection = new SelectionPanel();
        panelChoice = new ChoicePanel();
        panelInfos = new InfosEtResult();
        panelNavigation = new NavigationPanel();

        // Layout principal avec onglets
        JTabbedPane tabbedPane = new JTabbedPane();

        // Onglet 1: Planification
        JPanel panelPlanification = creerPanelPlanification();
        tabbedPane.addTab("📋 Planification", panelPlanification);

        // Onglet 2: Navigation
        tabbedPane.addTab("🗺️ Navigation", panelNavigation);

        // Onglet 3: Gestion Réparations
        GestionReparationPanel panelReparations = new GestionReparationPanel();
        tabbedPane.addTab("🔧 Réparations", panelReparations);

        // Onglet 4: Simulation Réparations
        SimulationReparationPanel panelSimulation = new SimulationReparationPanel();
        tabbedPane.addTab("🤖 Simulation Auto", panelSimulation);

        // Onglet 5: Gestion Précipitations
        PrecipitationManagementPanel panelPrecipitations = new PrecipitationManagementPanel();
        tabbedPane.addTab("⛈️ Précipitations", panelPrecipitations);

        add(tabbedPane);

        // Écoutants
        panelSelection.ajouterEcoutantBouton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                calculerTrajet();
            }
        });

        // Écoutant pour la sélection de chemin
        panelChoice.getTableChemins().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = panelChoice.getCheminSelectionne();
                if (selectedRow >= 0 && derniersResultats != null && selectedRow < derniersResultats.size()) {
                    ResultatTrajet resultat = derniersResultats.get(selectedRow);
                    panelInfos.afficherResultat(resultat);
                }
            }
        });

        setVisible(true);
    }

    private JPanel creerPanelPlanification() {
        JPanel panel = new JPanel(new BorderLayout());

        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPrincipal.setTopComponent(panelSelection);

        JSplitPane splitBas = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitBas.setLeftComponent(panelChoice);

        // Panel de droite avec infos et bouton navigation
        JPanel panelDroite = new JPanel(new BorderLayout());
        panelDroite.add(panelInfos, BorderLayout.CENTER);

        JPanel panelBoutonNav = new JPanel(new FlowLayout());
        JButton btnLancerNav = new JButton("🚗 Lancer la Navigation");
        btnLancerNav.setFont(new Font("Arial", Font.BOLD, 14));
        btnLancerNav.setBackground(new Color(0, 150, 0));
        btnLancerNav.setForeground(Color.WHITE);
        btnLancerNav.addActionListener(e -> lancerNavigation());
        panelBoutonNav.add(btnLancerNav);

        panelDroite.add(panelBoutonNav, BorderLayout.SOUTH);

        splitBas.setRightComponent(panelDroite);
        splitBas.setDividerLocation(500);

        splitPrincipal.setBottomComponent(splitBas);
        splitPrincipal.setDividerLocation(150);

        panel.add(splitPrincipal);
        return panel;
    }

    private void lancerNavigation() {
        ResultatTrajet resultat = panelInfos.getResultatSelectionne();
        if (resultat == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un trajet à naviguer",
                    "Aucun trajet sélectionné",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Vérifier que le trajet est conforme
        if (!resultat.isConforme()) {
            JOptionPane.showMessageDialog(this,
                    "❌ Navigation impossible!\n\n" +
                            "Le trajet sélectionné n'est pas conforme:\n" +
                            "- Le véhicule " + resultat.getVehicule().getNom() + " ne peut pas passer\n" +
                            "  sur certaines routes (largeur insuffisante).\n\n" +
                            "Veuillez sélectionner un autre trajet ou un autre véhicule.",
                    "Trajet non conforme",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Récupérer la vitesse moyenne utilisée pour le calcul
        double vitesseMoyenne = panelSelection.getVitesseMoyenne();

        // Charger le trajet dans le panel de navigation
        panelNavigation.chargerTrajet(resultat, vitesseMoyenne);

        // Basculer vers l'onglet navigation
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);
        tabbedPane.setSelectedIndex(1);

        JOptionPane.showMessageDialog(this,
                "✓ Navigation chargée!\n\n" +
                        "Vitesse moyenne configurée: " + String.format("%.1f km/h", vitesseMoyenne) + "\n" +
                        "Le véhicule ralentira automatiquement dans les zones endommagées.\n\n" +
                        "Cliquez sur '▶ Démarrer' pour commencer.",
                "Prêt à naviguer",
                JOptionPane.INFORMATION_MESSAGE);
    }

    // Dans la méthode calculerTrajet() de MainFrame.java, remplacer:

    private void calculerTrajet() {
        try {
            int idDepart = panelSelection.getIdPointDepart();
            int idArrivee = panelSelection.getIdPointArrivee();
            Vehicule vehicule = panelSelection.getVehiculeSelecte();
            double vitesseMoyenne = panelSelection.getVitesseMoyenne();
            Date heureDepart = panelSelection.getHeureDepart(); // NOUVEAU

            if (idDepart < 0 || idArrivee < 0 || vehicule == null || vitesseMoyenne <= 0 || heureDepart == null) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs correctement", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (idDepart == idArrivee) {
                JOptionPane.showMessageDialog(this,
                        "Le point de départ et d'arrivée doivent être différents",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JDialog loadingDialog = new JDialog(this, "Calcul en cours...", true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            loadingDialog.add(BorderLayout.CENTER, progressBar);
            loadingDialog.add(BorderLayout.NORTH, new JLabel("Recherche des meilleurs trajets..."));
            loadingDialog.setSize(300, 100);
            loadingDialog.setLocationRelativeTo(this);

            SwingWorker<List<ResultatTrajet>, Void> worker = new SwingWorker<List<ResultatTrajet>, Void>() {
                @Override
                protected List<ResultatTrajet> doInBackground() throws Exception {
                    return TrajetController.trouverCheminsLesPlusCourts(idDepart, idArrivee, vehicule,
                            vitesseMoyenne, heureDepart); // NOUVEAU: ajouter heureDepart
                }

                @Override
                protected void done() {
                    loadingDialog.dispose();
                    try {
                        List<ResultatTrajet> resultats = get();
                        derniersResultats = resultats;

                        if (resultats.isEmpty()) {
                            JOptionPane.showMessageDialog(MainFrame.this,
                                    "Aucun chemin disponible entre ces deux points",
                                    "Résultat", JOptionPane.INFORMATION_MESSAGE);
                            return;
                        }

                        panelChoice.afficherResultats(resultats);
                        panelInfos.afficherResultat(resultats.get(0));

                        // Message avec les horaires
                        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                        String message = String.format(
                                "%d trajet(s) trouvé(s)!\n\nMeilleur trajet:\n" +
                                        "- Distance: %.2f km\n" +
                                        "- Temps de route: %s\n" +
                                        "- Temps d'attente: %s\n" +
                                        "- Temps total: %s\n" +
                                        "- Départ: %s\n" +
                                        "- Arrivée: %s",
                                resultats.size(),
                                resultats.get(0).getDistanceTotaleKm(),
                                resultats.get(0).getTempsFormate(),
                                resultats.get(0).getTempsAttenteFormate(),
                                resultats.get(0).getTempsTotalAvecPausesFormate(),
                                sdf.format(resultats.get(0).getHeureDepart()),
                                sdf.format(resultats.get(0).getHeureArrivee()));

                        JOptionPane.showMessageDialog(MainFrame.this, message,
                                "Trajets calculés", JOptionPane.INFORMATION_MESSAGE);

                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Erreur lors du calcul: " + ex.getMessage(),
                                "Erreur", JOptionPane.ERROR_MESSAGE);
                        ex.printStackTrace();
                    }
                }
            };

            worker.execute();
            loadingDialog.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Erreur: " + e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        // Définir le Look and Feel du système
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame();
            }
        });
    }
}