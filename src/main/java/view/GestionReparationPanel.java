package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

import dao.MateriauDAO;
import dao.ReparationDAO;
import dao.CheminDAO;
import dao.DegatDAO;
import model.Materiau;
import model.Reparation;
import model.Chemin;
import model.Degat;

public class GestionReparationPanel extends JPanel {
    private JTabbedPane tabbedPane;

    // Onglet Matériaux
    private JTextField txtNomMateriau;
    private JTextArea txtDescMateriau;
    private JTable tableMateriaux;
    private DefaultTableModel modelMateriaux;
    private MateriauDAO materiauDAO;

    // Onglet Réparations
    private JComboBox<Materiau> cmbMateriau;
    private JTextField txtProfondeurMin;
    private JTextField txtProfondeurMax;
    private JTextField txtPrixM2;
    private JTextArea txtDescReparation;
    private JTable tableReparations;
    private DefaultTableModel modelReparations;
    private ReparationDAO reparationDAO;

    // Onglet Dégâts
    private JComboBox<Chemin> cmbChemin;
    private JComboBox<Materiau> cmbMateriauDegat;
    private JTextField txtPointKm;
    private JTextField txtSurfaceM2;
    private JTextField txtProfondeurM;
    private JTable tableDegats;
    private DefaultTableModel modelDegats;
    private CheminDAO cheminDAO;
    private DegatDAO degatDAO;

    public GestionReparationPanel() {
        materiauDAO = new MateriauDAO();
        reparationDAO = new ReparationDAO();
        cheminDAO = new CheminDAO();
        degatDAO = new DegatDAO();

        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Matériaux", creerPanelMateriaux());
        tabbedPane.addTab("Tarifs Réparations", creerPanelReparations());
        tabbedPane.addTab("Dégâts", creerPanelDegats());

        add(tabbedPane, BorderLayout.CENTER);

        chargerMateriaux();
        chargerReparations();
        rafraichirComboChemins();
        rafraichirComboMateriauxDegats();
        chargerDegats(null);
    }

    // ==================== ONGLET MATÉRIAUX ====================

    private JPanel creerPanelMateriaux() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire d'ajout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un Matériau"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Nom
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Nom * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtNomMateriau = new JTextField(20);
        formPanel.add(txtNomMateriau, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Description :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescMateriau = new JTextArea(3, 20);
        txtDescMateriau.setLineWrap(true);
        txtDescMateriau.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescMateriau);
        formPanel.add(scrollDesc, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAjouter = new JButton("Ajouter");
        btnAjouter.addActionListener(e -> ajouterMateriau());
        btnPanel.add(btnAjouter);

        JButton btnEffacer = new JButton("Effacer");
        btnEffacer.addActionListener(e -> {
            txtNomMateriau.setText("");
            txtDescMateriau.setText("");
        });
        btnPanel.add(btnEffacer);

        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table des matériaux
        String[] colonnes = { "ID", "Nom", "Description" };
        modelMateriaux = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableMateriaux = new JTable(modelMateriaux);
        tableMateriaux.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableMateriaux.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableMateriaux.getColumnModel().getColumn(2).setPreferredWidth(300);

        JScrollPane scrollTable = new JScrollPane(tableMateriaux);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Matériaux Existants"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Bouton supprimer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSupprimer = new JButton("Supprimer Sélectionné");
        btnSupprimer.addActionListener(e -> supprimerMateriau());
        bottomPanel.add(btnSupprimer);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void ajouterMateriau() {
        String nom = txtNomMateriau.getText().trim();
        String description = txtDescMateriau.getText().trim();

        if (nom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le nom du matériau est obligatoire",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Materiau materiau = new Materiau();
            materiau.setNom(nom);
            materiau.setDescription(description.isEmpty() ? null : description);

            int id = materiauDAO.inserer(materiau);

            JOptionPane.showMessageDialog(this, "Matériau ajouté avec succès (ID: " + id + ")",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            txtNomMateriau.setText("");
            txtDescMateriau.setText("");

            chargerMateriaux();
            rafraichirComboMateriaux();
            rafraichirComboMateriauxDegats();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerMateriau() {
        int selectedRow = tableMateriaux.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un matériau",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelMateriaux.getValueAt(selectedRow, 0);
        String nom = (String) modelMateriaux.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer le matériau '" + nom + "' ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                materiauDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Matériau supprimé avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerMateriaux();
                rafraichirComboMateriaux();
                rafraichirComboMateriauxDegats();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerMateriaux() {
        modelMateriaux.setRowCount(0);
        try {
            List<Materiau> materiaux = materiauDAO.getAll();
            for (Materiau m : materiaux) {
                modelMateriaux.addRow(new Object[] {
                        m.getId(),
                        m.getNom(),
                        m.getDescription()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== ONGLET RÉPARATIONS ====================

    private JPanel creerPanelReparations() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire d'ajout
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un Tarif de Réparation"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Matériau
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Matériau * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbMateriau = new JComboBox<>();
        rafraichirComboMateriaux();
        formPanel.add(cmbMateriau, gbc);

        // Profondeur min
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur Min (m) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProfondeurMin = new JTextField(10);
        txtProfondeurMin.setText("0");
        formPanel.add(txtProfondeurMin, gbc);

        // Profondeur max
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur Max (m) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProfondeurMax = new JTextField(10);
        txtProfondeurMax.setText("999999");
        JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        maxPanel.add(txtProfondeurMax);
        maxPanel.add(new JLabel("(utiliser 999999 pour ∞)"));
        formPanel.add(maxPanel, gbc);

        // Prix par m²
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Prix par m² (Ar) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPrixM2 = new JTextField(10);
        formPanel.add(txtPrixM2, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Description :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        txtDescReparation = new JTextArea(3, 20);
        txtDescReparation.setLineWrap(true);
        txtDescReparation.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDescReparation);
        formPanel.add(scrollDesc, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAjouter = new JButton("Ajouter");
        btnAjouter.addActionListener(e -> ajouterReparation());
        btnPanel.add(btnAjouter);

        JButton btnEffacer = new JButton("Effacer");
        btnEffacer.addActionListener(e -> {
            txtProfondeurMin.setText("0");
            txtProfondeurMax.setText("999999");
            txtPrixM2.setText("");
            txtDescReparation.setText("");
        });
        btnPanel.add(btnEffacer);

        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table des réparations
        String[] colonnes = { "ID", "Matériau", "Prof. Min (m)", "Prof. Max (m)", "Prix/m² (Ar)", "Description" };
        modelReparations = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableReparations = new JTable(modelReparations);
        tableReparations.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableReparations.getColumnModel().getColumn(1).setPreferredWidth(100);
        tableReparations.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableReparations.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableReparations.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableReparations.getColumnModel().getColumn(5).setPreferredWidth(250);

        JScrollPane scrollTable = new JScrollPane(tableReparations);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Tarifs Existants"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Bouton supprimer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSupprimer = new JButton("Supprimer Sélectionné");
        btnSupprimer.addActionListener(e -> supprimerReparation());
        bottomPanel.add(btnSupprimer);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void rafraichirComboMateriaux() {
        cmbMateriau.removeAllItems();
        try {
            List<Materiau> materiaux = materiauDAO.getAll();
            for (Materiau m : materiaux) {
                cmbMateriau.addItem(m);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des matériaux: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rafraichirComboMateriauxDegats() {
        if (cmbMateriauDegat == null)
            return;
        cmbMateriauDegat.removeAllItems();
        try {
            List<Materiau> materiaux = materiauDAO.getAll();
            for (Materiau m : materiaux) {
                cmbMateriauDegat.addItem(m);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des matériaux: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterReparation() {
        Materiau materiau = (Materiau) cmbMateriau.getSelectedItem();
        if (materiau == null) {
            JOptionPane.showMessageDialog(this, "Veuillez d'abord ajouter un matériau dans l'onglet 'Matériaux'",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double min = Double.parseDouble(txtProfondeurMin.getText().trim());
            double max = Double.parseDouble(txtProfondeurMax.getText().trim());
            double prix = Double.parseDouble(txtPrixM2.getText().trim());
            String description = txtDescReparation.getText().trim();

            Reparation reparation = new Reparation();
            reparation.setMateriauId(materiau.getId());
            reparation.setProfondeurMin(min);
            reparation.setProfondeurMax(max);
            reparation.setPrixParM2(prix);
            reparation.setDescription(description.isEmpty() ? null : description);

            int id = reparationDAO.inserer(reparation);

            JOptionPane.showMessageDialog(this, "Tarif de réparation ajouté avec succès (ID: " + id + ")",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            txtProfondeurMin.setText("0");
            txtProfondeurMax.setText("999999");
            txtPrixM2.setText("");
            txtDescReparation.setText("");

            chargerReparations();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ==================== ONGLET DÉGÂTS ====================

    private JPanel creerPanelDegats() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire d'ajout de dégât
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Ajouter un Dégât"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Chemin
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Chemin * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbChemin = new JComboBox<>();
        // Rendu pour afficher le nom du chemin
        cmbChemin.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Chemin) {
                    Chemin ch = (Chemin) value;
                    ((JLabel) c).setText(ch.getNom() + " (ID=" + ch.getId() + ")");
                }
                return c;
            }
        });
        formPanel.add(cmbChemin, gbc);

        // Matériau
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Matériau :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbMateriauDegat = new JComboBox<>();
        formPanel.add(cmbMateriauDegat, gbc);

        // Point km
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Point (km) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPointKm = new JTextField(10);
        formPanel.add(txtPointKm, gbc);

        // Surface m2
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Surface (m²) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtSurfaceM2 = new JTextField(10);
        formPanel.add(txtSurfaceM2, gbc);

        // Profondeur m
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur (m) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProfondeurM = new JTextField(10);
        formPanel.add(txtProfondeurM, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnAjouter = new JButton("Ajouter");
        btnAjouter.addActionListener(e -> ajouterDegat());
        btnPanel.add(btnAjouter);

        JButton btnEffacer = new JButton("Effacer");
        btnEffacer.addActionListener(e -> {
            txtPointKm.setText("");
            txtSurfaceM2.setText("");
            txtProfondeurM.setText("");
        });
        btnPanel.add(btnEffacer);

        formPanel.add(btnPanel, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Table des dégâts
        String[] colonnes = { "ID", "Chemin", "Point (km)", "Surface (m²)", "Profondeur (m)", "Matériau" };
        modelDegats = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableDegats = new JTable(modelDegats);
        tableDegats.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableDegats.getColumnModel().getColumn(1).setPreferredWidth(150);
        tableDegats.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableDegats.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableDegats.getColumnModel().getColumn(4).setPreferredWidth(100);
        tableDegats.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollTable = new JScrollPane(tableDegats);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Dégâts"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Bouton supprimer
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSupprimer = new JButton("Supprimer Sélectionné");
        btnSupprimer.addActionListener(e -> supprimerDegat());
        bottomPanel.add(btnSupprimer);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Rechargement des dégâts lorsque le chemin change
        cmbChemin.addActionListener(e -> {
            Chemin c = (Chemin) cmbChemin.getSelectedItem();
            if (c != null) {
                chargerDegats(c.getId());
            } else {
                chargerDegats(null);
            }
        });

        return panel;
    }

    private void rafraichirComboChemins() {
        if (cmbChemin == null)
            return;
        cmbChemin.removeAllItems();
        try {
            List<Chemin> chemins = cheminDAO.getAll();
            for (Chemin c : chemins) {
                cmbChemin.addItem(c);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des chemins: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ajouterDegat() {
        Chemin chemin = (Chemin) cmbChemin.getSelectedItem();
        if (chemin == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un chemin",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double pointKm = Double.parseDouble(txtPointKm.getText().trim());
            double surface = Double.parseDouble(txtSurfaceM2.getText().trim());
            double profondeur = Double.parseDouble(txtProfondeurM.getText().trim());
            Materiau materiau = (Materiau) cmbMateriauDegat.getSelectedItem();

            if (pointKm < 0) {
                JOptionPane.showMessageDialog(this, "Le point (km) doit être >= 0",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (surface <= 0) {
                JOptionPane.showMessageDialog(this, "La surface (m²) doit être > 0",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (profondeur < 0) {
                JOptionPane.showMessageDialog(this, "La profondeur (m) doit être >= 0",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Degat degat = new Degat();
            degat.setCheminId(chemin.getId());
            degat.setPointKm(pointKm);
            degat.setSurfaceM2(surface);
            degat.setProfondeurM(profondeur);
            degat.setMateriauId(materiau != null ? materiau.getId() : null);

            int id = degatDAO.inserer(degat);

            JOptionPane.showMessageDialog(this, "Dégât ajouté avec succès (ID: " + id + ")",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            txtPointKm.setText("");
            txtSurfaceM2.setText("");
            txtProfondeurM.setText("");

            chargerDegats(chemin.getId());

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir des valeurs numériques valides",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerDegat() {
        int selectedRow = tableDegats.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un dégât",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelDegats.getValueAt(selectedRow, 0);
        String cheminNom = (String) modelDegats.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer ce dégât sur '" + cheminNom + "' ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                degatDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Dégât supprimé avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                Chemin c = (Chemin) cmbChemin.getSelectedItem();
                chargerDegats(c != null ? c.getId() : null);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerDegats(Integer cheminId) {
        modelDegats.setRowCount(0);
        try {
            List<Degat> degats;
            if (cheminId != null) {
                degats = degatDAO.getParChemin(cheminId);
            } else {
                degats = degatDAO.getAll();
            }

            for (Degat d : degats) {
                String cheminNom;
                try {
                    Chemin ch = cheminDAO.getById(d.getCheminId());
                    cheminNom = ch != null ? ch.getNom() : ("Chemin #" + d.getCheminId());
                } catch (SQLException e) {
                    cheminNom = "Chemin #" + d.getCheminId();
                }

                String materiauNom = "";
                if (d.getMateriauId() != null) {
                    try {
                        Materiau m = materiauDAO.getById(d.getMateriauId());
                        materiauNom = m != null ? m.getNom() : ("ID=" + d.getMateriauId());
                    } catch (SQLException e) {
                        materiauNom = "ID=" + d.getMateriauId();
                    }
                }

                modelDegats.addRow(new Object[] {
                        d.getId(),
                        cheminNom,
                        String.format("%.2f", d.getPointKm()),
                        String.format("%.2f", d.getSurfaceM2()),
                        String.format("%.2f", d.getProfondeurM()),
                        materiauNom
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerReparation() {
        int selectedRow = tableReparations.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un tarif",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelReparations.getValueAt(selectedRow, 0);
        String materiau = (String) modelReparations.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer ce tarif pour '" + materiau + "' ?",
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                reparationDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Tarif supprimé avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerReparations();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void chargerReparations() {
        modelReparations.setRowCount(0);
        try {
            List<Reparation> reparations = reparationDAO.getAll();
            for (Reparation r : reparations) {
                String maxDisplay = r.getProfondeurMax() >= 999999 ? "∞" : String.format("%.2f", r.getProfondeurMax());
                modelReparations.addRow(new Object[] {
                        r.getId(),
                        r.getMateriauNom(),
                        String.format("%.2f", r.getProfondeurMin()),
                        maxDisplay,
                        String.format("%.2f", r.getPrixParM2()),
                        r.getDescription()
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
