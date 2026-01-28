package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import dao.*;
import model.*;
import controller.MaterialSelectionService;

public class SimulationReparationPanel extends JPanel {
    private JComboBox<Chemin> cmbChemin;
    private JTable tableSimulation;
    private DefaultTableModel modelSimulation;
    private JLabel lblCoutTotal;
    private JLabel lblNbDegats;
    private JTextArea txtDetails;

    private CheminDAO cheminDAO;
    private DegatDAO degatDAO;
    private ReparationDegatDAO reparationDegatDAO;
    private MaterialSelectionService materialService;

    public SimulationReparationPanel() {
        cheminDAO = new CheminDAO();
        degatDAO = new DegatDAO();
        reparationDegatDAO = new ReparationDegatDAO();
        materialService = new MaterialSelectionService();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel du haut : Sélection et infos
        JPanel topPanel = creerPanelSelection();
        add(topPanel, BorderLayout.NORTH);

        // Panel central : Table de simulation
        JPanel centerPanel = creerPanelTable();
        add(centerPanel, BorderLayout.CENTER);

        // Panel du bas : Détails et résumé
        JPanel bottomPanel = creerPanelResume();
        add(bottomPanel, BorderLayout.SOUTH);

        rafraichirComboChemins();
    }

    private JPanel creerPanelSelection() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("🤖 Simulation Automatique des Réparations"));

        panel.add(new JLabel("Chemin :"));
        cmbChemin = new JComboBox<>();
        cmbChemin.setPreferredSize(new Dimension(300, 25));
        panel.add(cmbChemin);

        JButton btnSimuler = new JButton("🔄 Simuler les Réparations");
        btnSimuler.setFont(new Font("Arial", Font.BOLD, 12));
        btnSimuler.setBackground(new Color(70, 130, 180));
        btnSimuler.setForeground(Color.WHITE);
        btnSimuler.addActionListener(e -> simulerReparations());
        panel.add(btnSimuler);

        JButton btnSimulerTout = new JButton("🌍 Simuler Tous les Chemins");
        btnSimulerTout.setFont(new Font("Arial", Font.BOLD, 12));
        btnSimulerTout.setBackground(new Color(34, 139, 34));
        btnSimulerTout.setForeground(Color.WHITE);
        btnSimulerTout.addActionListener(e -> simulerToutesReparations());
        panel.add(btnSimulerTout);

        return panel;
    }

    private JPanel creerPanelTable() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] colonnes = {
                "Chemin", "Point (km)", "Surface (m²)", "Profondeur (m)",
                "Précipitation (mm)", "Matériau", "Coût (Ar)"
        };

        modelSimulation = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableSimulation = new JTable(modelSimulation);
        tableSimulation.setRowHeight(25);
        tableSimulation.getColumnModel().getColumn(0).setPreferredWidth(180);
        tableSimulation.getColumnModel().getColumn(1).setPreferredWidth(80);
        tableSimulation.getColumnModel().getColumn(2).setPreferredWidth(90);
        tableSimulation.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableSimulation.getColumnModel().getColumn(4).setPreferredWidth(120);
        tableSimulation.getColumnModel().getColumn(5).setPreferredWidth(120);
        tableSimulation.getColumnModel().getColumn(6).setPreferredWidth(100);

        JScrollPane scrollPane = new JScrollPane(tableSimulation);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Simulation des Réparations selon Précipitations"));
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel creerPanelResume() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Panel résumé avec statistiques
        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        statsPanel.setBorder(BorderFactory.createTitledBorder("📊 Résumé"));

        statsPanel.add(new JLabel("Nombre de dégâts :"));
        lblNbDegats = new JLabel("0");
        lblNbDegats.setFont(new Font("Arial", Font.BOLD, 14));
        statsPanel.add(lblNbDegats);

        statsPanel.add(new JSeparator(SwingConstants.VERTICAL));

        statsPanel.add(new JLabel("Coût Total :"));
        lblCoutTotal = new JLabel("0.00 Ar");
        lblCoutTotal.setFont(new Font("Arial", Font.BOLD, 16));
        lblCoutTotal.setForeground(new Color(220, 20, 60));
        statsPanel.add(lblCoutTotal);

        panel.add(statsPanel, BorderLayout.NORTH);

        // Zone de détails
        txtDetails = new JTextArea(5, 50);
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtDetails.setBackground(new Color(245, 245, 245));
        JScrollPane scrollDetails = new JScrollPane(txtDetails);
        scrollDetails.setBorder(BorderFactory.createTitledBorder("Détails de la Simulation"));
        panel.add(scrollDetails, BorderLayout.CENTER);

        return panel;
    }

    private void simulerReparations() {
        Chemin chemin = (Chemin) cmbChemin.getSelectedItem();
        if (chemin == null) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un chemin",
                    "Erreur", JOptionPane.WARNING_MESSAGE);
            return;
        }

        modelSimulation.setRowCount(0);
        StringBuilder details = new StringBuilder();
        details.append("=== SIMULATION POUR : ").append(chemin.getNom()).append(" ===\n\n");

        try {
            List<Degat> degats = degatDAO.getParChemin(chemin.getId());

            if (degats.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Aucun dégât trouvé sur ce chemin",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                txtDetails.setText("Aucun dégât à réparer sur ce chemin.");
                return;
            }

            double coutTotal = 0.0;
            int nbSuccess = 0;
            int nbEchec = 0;

            for (Degat degat : degats) {
                try {
                    // Obtenir le matériau recommandé
                    Materiau materiau = materialService.getMaterialForDommage(
                            degat.getCheminId(),
                            degat.getPointKm());

                    // Calculer le coût
                    double cout = reparationDegatDAO.calculerCoutReparation(
                            degat.getId(),
                            materiau.getId());

                    // Obtenir le niveau de précipitation
                    PrecipitationDAO precipDAO = new PrecipitationDAO();
                    Precipitation precip = precipDAO.findByCheminAndKm(
                            degat.getCheminId(),
                            degat.getPointKm());
                    double niveauPrecip = precip != null ? precip.getNiveauMm() : 0.0;

                    // Ajouter à la table
                    modelSimulation.addRow(new Object[] {
                            chemin.getNom(),
                            String.format("%.2f", degat.getPointKm()),
                            String.format("%.2f", degat.getSurfaceM2()),
                            String.format("%.3f", degat.getProfondeurM()),
                            String.format("%.3f", niveauPrecip),
                            materiau.getNom(),
                            String.format("%.2f", cout)
                    });

                    coutTotal += cout;
                    nbSuccess++;

                    details.append(String.format("✓ Dégât au km %.2f : %s (%.2f Ar)\n",
                            degat.getPointKm(), materiau.getNom(), cout));

                } catch (SQLException e) {
                    nbEchec++;
                    details.append(String.format("✗ Dégât au km %.2f : ERREUR - %s\n",
                            degat.getPointKm(), e.getMessage()));
                }
            }

            lblNbDegats.setText(String.valueOf(nbSuccess + nbEchec));
            lblCoutTotal.setText(String.format("%.2f Ar", coutTotal));

            details.append(String.format("\n--- RÉSUMÉ ---\n"));
            details.append(String.format("Réparations simulées : %d\n", nbSuccess));
            details.append(String.format("Échecs : %d\n", nbEchec));
            details.append(String.format("Coût total : %.2f Ar\n", coutTotal));

            txtDetails.setText(details.toString());

            if (nbEchec > 0) {
                JOptionPane.showMessageDialog(this,
                        String.format(
                                "⚠️ Simulation terminée avec %d erreur(s).\n\nVérifiez la configuration des matériaux-précipitations.",
                                nbEchec),
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        String.format("✅ Simulation réussie!\n\n%d réparations calculées\nCoût total : %.2f Ar",
                                nbSuccess, coutTotal),
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la simulation : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            txtDetails.setText("Erreur : " + e.getMessage());
        }
    }

    private void simulerToutesReparations() {
        modelSimulation.setRowCount(0);
        StringBuilder details = new StringBuilder();
        details.append("=== SIMULATION GLOBALE DE TOUS LES CHEMINS ===\n\n");

        try {
            List<Chemin> chemins = cheminDAO.getAll();

            double coutTotalGlobal = 0.0;
            int nbDegatsTotal = 0;
            int nbSuccessTotal = 0;
            int nbEchecTotal = 0;

            for (Chemin chemin : chemins) {
                List<Degat> degats = degatDAO.getParChemin(chemin.getId());

                if (degats.isEmpty()) {
                    continue;
                }

                details.append(String.format("--- %s ---\n", chemin.getNom()));

                for (Degat degat : degats) {
                    nbDegatsTotal++;

                    try {
                        Materiau materiau = materialService.getMaterialForDommage(
                                degat.getCheminId(),
                                degat.getPointKm());

                        double cout = reparationDegatDAO.calculerCoutReparation(
                                degat.getId(),
                                materiau.getId());

                        PrecipitationDAO precipDAO = new PrecipitationDAO();
                        Precipitation precip = precipDAO.findByCheminAndKm(
                                degat.getCheminId(),
                                degat.getPointKm());
                        double niveauPrecip = precip != null ? precip.getNiveauMm() : 0.0;

                        modelSimulation.addRow(new Object[] {
                                chemin.getNom(),
                                String.format("%.2f", degat.getPointKm()),
                                String.format("%.2f", degat.getSurfaceM2()),
                                String.format("%.3f", degat.getProfondeurM()),
                                String.format("%.3f", niveauPrecip),
                                materiau.getNom(),
                                String.format("%.2f", cout)
                        });

                        coutTotalGlobal += cout;
                        nbSuccessTotal++;

                        details.append(String.format("  ✓ km %.2f : %s (%.2f Ar)\n",
                                degat.getPointKm(), materiau.getNom(), cout));

                    } catch (SQLException e) {
                        nbEchecTotal++;
                        details.append(String.format("  ✗ km %.2f : ERREUR\n", degat.getPointKm()));
                    }
                }

                details.append("\n");
            }

            lblNbDegats.setText(String.valueOf(nbDegatsTotal));
            lblCoutTotal.setText(String.format("%.2f Ar", coutTotalGlobal));

            details.append(String.format("======== RÉSUMÉ GLOBAL ========\n"));
            details.append(String.format("Chemins traités : %d\n", chemins.size()));
            details.append(String.format("Dégâts totaux : %d\n", nbDegatsTotal));
            details.append(String.format("Réparations simulées : %d\n", nbSuccessTotal));
            details.append(String.format("Échecs : %d\n", nbEchecTotal));
            details.append(String.format("COÛT TOTAL : %.2f Ar\n", coutTotalGlobal));

            txtDetails.setText(details.toString());

            if (nbEchecTotal > 0) {
                JOptionPane.showMessageDialog(this,
                        String.format("⚠️ Simulation globale terminée avec %d erreur(s).", nbEchecTotal),
                        "Avertissement", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        String.format("✅ Simulation globale réussie!\n\n%d réparations calculées\nCoût total : %.2f Ar",
                                nbSuccessTotal, coutTotalGlobal),
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors de la simulation globale : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            txtDetails.setText("Erreur : " + e.getMessage());
        }
    }

    private void rafraichirComboChemins() {
        cmbChemin.removeAllItems();
        try {
            List<Chemin> chemins = cheminDAO.getAll();
            for (Chemin c : chemins) {
                cmbChemin.addItem(c);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des chemins",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
