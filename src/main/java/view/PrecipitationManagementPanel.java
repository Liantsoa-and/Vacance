package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import dao.PrecipitationDAO;
import dao.CheminDAO;
import dao.MateriauPrecipitationDAO;
import dao.MateriauDAO;
import model.Precipitation;
import model.Chemin;
import model.MateriauPrecipitation;
import model.Materiau;

public class PrecipitationManagementPanel extends JPanel {
    private JTabbedPane tabbedPane;

    // Onglet Zones de Précipitation
    private JComboBox<Chemin> cmbChemin;
    private JTextField txtDebutKm;
    private JTextField txtFinKm;
    private JTextField txtNiveauMm;
    private JTable tablePrecipitations;
    private DefaultTableModel modelPrecipitations;
    private PrecipitationDAO precipitationDAO;
    private CheminDAO cheminDAO;

    // Onglet Configuration Matériaux-Précipitations
    private JComboBox<Materiau> cmbMateriau;
    private JTextField txtNiveauMin;
    private JTextField txtNiveauMax;
    private JTable tableMateriauPrecipitation;
    private DefaultTableModel modelMateriauPrecipitation;
    private MateriauPrecipitationDAO materiauPrecipitationDAO;
    private MateriauDAO materiauDAO;

    public PrecipitationManagementPanel() {
        precipitationDAO = new PrecipitationDAO();
        cheminDAO = new CheminDAO();
        materiauPrecipitationDAO = new MateriauPrecipitationDAO();
        materiauDAO = new MateriauDAO();

        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Zones de Précipitation", creerPanelPrecipitations());
        tabbedPane.addTab("Configuration Matériaux", creerPanelMateriauPrecipitation());

        add(tabbedPane, BorderLayout.CENTER);

        rafraichirComboChemins();
        chargerPrecipitations();
        rafraichirComboMateriaux();
        chargerMateriauPrecipitations();
    }

    // ==================== ONGLET ZONES DE PRÉCIPITATION ====================

    private JPanel creerPanelPrecipitations() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire d'ajout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter une Zone de Précipitation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Chemin
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Chemin :"), gbc);
        gbc.gridx = 1;
        cmbChemin = new JComboBox<>();
        formPanel.add(cmbChemin, gbc);

        // Début km
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Début (km) :"), gbc);
        gbc.gridx = 1;
        txtDebutKm = new JTextField(10);
        formPanel.add(txtDebutKm, gbc);

        // Fin km
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Fin (km) :"), gbc);
        gbc.gridx = 1;
        txtFinKm = new JTextField(10);
        formPanel.add(txtFinKm, gbc);

        // Niveau mm
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Niveau (mm) :"), gbc);
        gbc.gridx = 1;
        txtNiveauMm = new JTextField(10);
        formPanel.add(txtNiveauMm, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAjouter = new JButton("Ajouter");
        JButton btnSupprimer = new JButton("Supprimer");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnSupprimer);
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table
        String[] colonnes = { "ID", "Chemin", "Début km", "Fin km", "Niveau (mm)" };
        modelPrecipitations = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablePrecipitations = new JTable(modelPrecipitations);
        JScrollPane scrollPane = new JScrollPane(tablePrecipitations);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Règles"));
        JTextArea infoText = new JTextArea(
                "⚠️ Les zones de précipitation ne doivent PAS se chevaucher sur un même chemin.\n" +
                        "✓ Zones valides : [2km→4km], [6km→8km] (gap entre 4 et 6)\n" +
                        "✗ Zones invalides : [2km→6km], [4km→8km] (chevauchement)");
        infoText.setEditable(false);
        infoText.setBackground(new Color(255, 255, 220));
        infoText.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(infoText, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        // Actions
        btnAjouter.addActionListener(e -> ajouterPrecipitation());
        btnSupprimer.addActionListener(e -> supprimerPrecipitation());

        return panel;
    }

    private void ajouterPrecipitation() {
        try {
            Chemin chemin = (Chemin) cmbChemin.getSelectedItem();
            if (chemin == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un chemin", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double debutKm = Double.parseDouble(txtDebutKm.getText().trim());
            double finKm = Double.parseDouble(txtFinKm.getText().trim());
            double niveauMm = Double.parseDouble(txtNiveauMm.getText().trim());

            if (debutKm >= finKm) {
                JOptionPane.showMessageDialog(this, "Le début doit être inférieur à la fin", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Precipitation p = new Precipitation();
            p.setCheminId(chemin.getId());
            p.setDebutKm(debutKm);
            p.setFinKm(finKm);
            p.setNiveauMm(niveauMm);

            if (precipitationDAO.create(p)) {
                JOptionPane.showMessageDialog(this, "Zone de précipitation ajoutée avec succès", "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                viderChampsZone();
                chargerPrecipitations();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs numériques valides", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("Chevauchement")) {
                JOptionPane.showMessageDialog(this,
                        "⚠️ Chevauchement détecté!\n\nCette zone chevauche une zone existante.", "Erreur de validation",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimerPrecipitation() {
        int selectedRow = tablePrecipitations.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une zone à supprimer", "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelPrecipitations.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Confirmer la suppression de cette zone ?", "Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (precipitationDAO.delete(id)) {
                    JOptionPane.showMessageDialog(this, "Zone supprimée avec succès", "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    chargerPrecipitations();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerPrecipitations() {
        modelPrecipitations.setRowCount(0);
        try {
            List<Precipitation> precipitations = precipitationDAO.findAll();
            for (Precipitation p : precipitations) {
                try {
                    Chemin chemin = cheminDAO.getById(p.getCheminId());
                    String nomChemin = chemin != null ? chemin.getNom() : "Inconnu";
                    modelPrecipitations.addRow(new Object[] {
                            p.getId(),
                            nomChemin,
                            String.format("%.3f", p.getDebutKm()),
                            String.format("%.3f", p.getFinKm()),
                            String.format("%.3f", p.getNiveauMm())
                    });
                } catch (SQLException ex) {
                    modelPrecipitations.addRow(new Object[] {
                            p.getId(),
                            "Erreur",
                            p.getDebutKm(),
                            p.getFinKm(),
                            p.getNiveauMm()
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement : " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des chemins", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viderChampsZone() {
        txtDebutKm.setText("");
        txtFinKm.setText("");
        txtNiveauMm.setText("");
    }

    // ==================== ONGLET MATÉRIAUX-PRÉCIPITATION ====================

    private JPanel creerPanelMateriauPrecipitation() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Configuration Matériau ↔ Précipitation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Matériau
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Matériau :"), gbc);
        gbc.gridx = 1;
        cmbMateriau = new JComboBox<>();
        formPanel.add(cmbMateriau, gbc);

        // Niveau min
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Niveau Min (mm) :"), gbc);
        gbc.gridx = 1;
        txtNiveauMin = new JTextField(10);
        formPanel.add(txtNiveauMin, gbc);

        // Niveau max
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Niveau Max (mm) :"), gbc);
        gbc.gridx = 1;
        txtNiveauMax = new JTextField(10);
        formPanel.add(txtNiveauMax, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnAjouter = new JButton("Ajouter Configuration");
        JButton btnSupprimer = new JButton("Supprimer");
        btnPanel.add(btnAjouter);
        btnPanel.add(btnSupprimer);
        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table
        String[] colonnes = { "ID", "Matériau", "Niveau Min (mm)", "Niveau Max (mm)", "Intervalle" };
        modelMateriauPrecipitation = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMateriauPrecipitation = new JTable(modelMateriauPrecipitation);
        JScrollPane scrollPane = new JScrollPane(tableMateriauPrecipitation);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Info panel
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Règles"));
        JTextArea infoText = new JTextArea(
                "🔧 Configuration des intervalles de précipitation pour chaque matériau.\n" +
                        "📊 Format intervalle : ]min; max] (ouvert à gauche, fermé à droite)\n" +
                        "✓ Exemple : Béton ]0; 0.2], Pavé ]0.2; 0.5], Asphalte ]0.5; 1.0]\n" +
                        "⚠️ Les intervalles doivent se couvrir sans chevauchement ni gap.");
        infoText.setEditable(false);
        infoText.setBackground(new Color(220, 240, 255));
        infoText.setFont(new Font("Arial", Font.PLAIN, 11));
        infoPanel.add(infoText, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);

        // Actions
        btnAjouter.addActionListener(e -> ajouterMateriauPrecipitation());
        btnSupprimer.addActionListener(e -> supprimerMateriauPrecipitation());

        return panel;
    }

    private void ajouterMateriauPrecipitation() {
        try {
            Materiau materiau = (Materiau) cmbMateriau.getSelectedItem();
            if (materiau == null) {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un matériau", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            double niveauMin = Double.parseDouble(txtNiveauMin.getText().trim());
            double niveauMax = Double.parseDouble(txtNiveauMax.getText().trim());

            if (niveauMin >= niveauMax) {
                JOptionPane.showMessageDialog(this, "Le niveau minimum doit être inférieur au maximum", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            MateriauPrecipitation mp = new MateriauPrecipitation();
            mp.setMateriauId(materiau.getId());
            mp.setNiveauMin(niveauMin);
            mp.setNiveauMax(niveauMax);

            if (materiauPrecipitationDAO.create(mp)) {
                JOptionPane.showMessageDialog(this, "Configuration ajoutée avec succès", "Succès",
                        JOptionPane.INFORMATION_MESSAGE);
                viderChampsConfig();
                chargerMateriauPrecipitations();
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez entrer des valeurs numériques valides", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            if (ex.getMessage().contains("invalide") || ex.getMessage().contains("chevauchement")) {
                JOptionPane.showMessageDialog(this, "⚠️ Configuration invalide!\n\n" + ex.getMessage(),
                        "Erreur de validation", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void supprimerMateriauPrecipitation() {
        int selectedRow = tableMateriauPrecipitation.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une configuration à supprimer", "Erreur",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelMateriauPrecipitation.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Confirmer la suppression de cette configuration ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                if (materiauPrecipitationDAO.delete(id)) {
                    JOptionPane.showMessageDialog(this, "Configuration supprimée avec succès", "Succès",
                            JOptionPane.INFORMATION_MESSAGE);
                    chargerMateriauPrecipitations();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + ex.getMessage(), "Erreur",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerMateriauPrecipitations() {
        modelMateriauPrecipitation.setRowCount(0);
        try {
            List<MateriauPrecipitation> configs = materiauPrecipitationDAO.findAll();
            for (MateriauPrecipitation mp : configs) {
                try {
                    Materiau materiau = materiauDAO.getById(mp.getMateriauId());
                    String nomMateriau = materiau != null ? materiau.getNom() : "Inconnu";
                    String intervalle = String.format("]%.3f; %.3f]", mp.getNiveauMin(), mp.getNiveauMax());

                    modelMateriauPrecipitation.addRow(new Object[] {
                            mp.getId(),
                            nomMateriau,
                            String.format("%.3f", mp.getNiveauMin()),
                            String.format("%.3f", mp.getNiveauMax()),
                            intervalle
                    });
                } catch (SQLException ex) {
                    modelMateriauPrecipitation.addRow(new Object[] {
                            mp.getId(),
                            "Erreur",
                            mp.getNiveauMin(),
                            mp.getNiveauMax(),
                            "Erreur"
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement : " + ex.getMessage(), "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rafraichirComboMateriaux() {
        cmbMateriau.removeAllItems();
        try {
            List<Materiau> materiaux = materiauDAO.getAll();
            for (Materiau m : materiaux) {
                cmbMateriau.addItem(m);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des matériaux", "Erreur",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viderChampsConfig() {
        txtNiveauMin.setText("");
        txtNiveauMax.setText("");
    }
}
