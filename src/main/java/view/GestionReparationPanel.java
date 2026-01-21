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
import dao.ReparationDegatDAO;
import model.Materiau;
import model.Reparation;
import model.Chemin;
import model.Degat;
import model.ReparationDegat;

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
    private JTextField txtPointKm;
    private JTextField txtSurfaceM2;
    private JTextField txtProfondeurM;
    private JTable tableDegats;
    private DefaultTableModel modelDegats;
    private CheminDAO cheminDAO;
    private DegatDAO degatDAO;

    // Modification de point kilométrique
    private JComboBox<Degat> cmbDegatModifier;
    private JTextField txtNouveauPointKm;

    // Onglet Réparations de Dégâts
    private JComboBox<Degat> cmbDegatReparation;
    private JComboBox<Materiau> cmbMateriauReparation;
    private JTable tableReparationsDegats;
    private DefaultTableModel modelReparationsDegats;
    private ReparationDegatDAO reparationDegatDAO;

    // Récapitulatif
    private JLabel lblCoutTotal;

    public GestionReparationPanel() {
        materiauDAO = new MateriauDAO();
        reparationDAO = new ReparationDAO();
        cheminDAO = new CheminDAO();
        degatDAO = new DegatDAO();
        reparationDegatDAO = new ReparationDegatDAO();

        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Matériaux", creerPanelMateriaux());
        tabbedPane.addTab("Tarifs Réparations", creerPanelReparations());
        tabbedPane.addTab("Dégâts", creerPanelDegats());
        tabbedPane.addTab("Réparations", creerPanelReparationsDegats());

        add(tabbedPane, BorderLayout.CENTER);

        chargerMateriaux();
        chargerReparations();
        rafraichirComboChemins();
        chargerDegats(null);
        rafraichirComboDegats();
        rafraichirComboMateriauxReparation();
        chargerReparationsDegats();
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
            rafraichirComboMateriauxReparation();

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
                rafraichirComboMateriauxReparation();
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

        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));

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

        // Point km
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Point (km) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtPointKm = new JTextField(10);
        formPanel.add(txtPointKm, gbc);

        // Surface m2
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Surface (m²) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtSurfaceM2 = new JTextField(10);
        formPanel.add(txtSurfaceM2, gbc);

        // Profondeur m
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Profondeur (m) * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        txtProfondeurM = new JTextField(10);
        formPanel.add(txtProfondeurM, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 4;
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
        topPanel.add(formPanel);

        // Formulaire de modification des points
        JPanel modifPanel = new JPanel(new GridBagLayout());
        modifPanel.setBorder(BorderFactory.createTitledBorder("Modifier Point Kilométrique"));
        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.insets = new Insets(5, 5, 5, 5);
        gbc2.fill = GridBagConstraints.HORIZONTAL;

        // Dégât à modifier
        gbc2.gridx = 0;
        gbc2.gridy = 0;
        gbc2.weightx = 0;
        modifPanel.add(new JLabel("Dégât :"), gbc2);
        gbc2.gridx = 1;
        gbc2.weightx = 1.0;
        cmbDegatModifier = new JComboBox<>();
        cmbDegatModifier.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Degat) {
                    Degat d = (Degat) value;
                    ((JLabel) c).setText(d.toString());
                }
                return c;
            }
        });
        modifPanel.add(cmbDegatModifier, gbc2);

        // Nouveau point
        gbc2.gridx = 0;
        gbc2.gridy = 1;
        gbc2.weightx = 0;
        modifPanel.add(new JLabel("Nouveau point (km) :"), gbc2);
        gbc2.gridx = 1;
        gbc2.weightx = 1.0;
        txtNouveauPointKm = new JTextField(10);
        modifPanel.add(txtNouveauPointKm, gbc2);

        // Bouton modifier
        gbc2.gridx = 0;
        gbc2.gridy = 2;
        gbc2.gridwidth = 2;
        gbc2.fill = GridBagConstraints.NONE;
        JButton btnModifier = new JButton("Modifier Point");
        btnModifier.addActionListener(e -> modifierPointDegat());
        modifPanel.add(btnModifier, gbc2);

        topPanel.add(modifPanel);
        panel.add(topPanel, BorderLayout.NORTH);

        // Table des dégâts (sans colonne matériau)
        String[] colonnes = { "ID", "Chemin", "Point (km)", "Surface (m²)", "Profondeur (m)" };
        modelDegats = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableDegats = new JTable(modelDegats);
        tableDegats.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableDegats.getColumnModel().getColumn(1).setPreferredWidth(200);
        tableDegats.getColumnModel().getColumn(2).setPreferredWidth(100);
        tableDegats.getColumnModel().getColumn(3).setPreferredWidth(120);
        tableDegats.getColumnModel().getColumn(4).setPreferredWidth(120);

        JScrollPane scrollTable = new JScrollPane(tableDegats);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Dégâts Enregistrés"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Panel en bas avec boutons
        JPanel bottomPanel = new JPanel(new FlowLayout());

        JButton btnSupprimer = new JButton("Supprimer Sélectionné");
        btnSupprimer.addActionListener(e -> supprimerDegat());
        bottomPanel.add(btnSupprimer);

        JButton btnRecapitulatif = new JButton("Voir Récapitulatif Réparations");
        btnRecapitulatif.addActionListener(e -> afficherRecapitulatif());
        bottomPanel.add(btnRecapitulatif);

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

    // ==================== ONGLET RÉPARATIONS DE DÉGÂTS ====================

    private JPanel creerPanelReparationsDegats() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulaire d'association dégât-matériau
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Associer Dégât et Matériau"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Dégât
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Dégât * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbDegatReparation = new JComboBox<>();
        cmbDegatReparation.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Degat) {
                    try {
                        Degat d = (Degat) value;
                        Chemin ch = cheminDAO.getById(d.getCheminId());
                        String cheminNom = ch != null ? ch.getNom() : "Chemin #" + d.getCheminId();
                        ((JLabel) c).setText(String.format("%s - Point %.2f km (%.2f m², %.2f m prof.)",
                                cheminNom, d.getPointKm(), d.getSurfaceM2(), d.getProfondeurM()));
                    } catch (Exception ex) {
                        ((JLabel) c).setText(value.toString());
                    }
                }
                return c;
            }
        });
        formPanel.add(cmbDegatReparation, gbc);

        // Matériau
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        formPanel.add(new JLabel("Matériau * :"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        cmbMateriauReparation = new JComboBox<>();
        formPanel.add(cmbMateriauReparation, gbc);

        // Boutons
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        JPanel btnPanel = new JPanel(new FlowLayout());

        JButton btnAssocier = new JButton("Associer Réparation");
        btnAssocier.addActionListener(e -> associerReparation());
        btnPanel.add(btnAssocier);

        JButton btnValiderTout = new JButton("Valider Toutes les Réparations");
        btnValiderTout.addActionListener(e -> validerToutesReparations());
        btnPanel.add(btnValiderTout);

        formPanel.add(btnPanel, gbc);
        panel.add(formPanel, BorderLayout.NORTH);

        // Table des réparations de dégâts
        String[] colonnes = { "ID", "Dégât", "Matériau", "Coût (Ar)", "Validée" };
        modelReparationsDegats = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableReparationsDegats = new JTable(modelReparationsDegats);
        tableReparationsDegats.getColumnModel().getColumn(0).setPreferredWidth(50);
        tableReparationsDegats.getColumnModel().getColumn(1).setPreferredWidth(250);
        tableReparationsDegats.getColumnModel().getColumn(2).setPreferredWidth(120);
        tableReparationsDegats.getColumnModel().getColumn(3).setPreferredWidth(100);
        tableReparationsDegats.getColumnModel().getColumn(4).setPreferredWidth(80);

        JScrollPane scrollTable = new JScrollPane(tableReparationsDegats);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Réparations Programmées"));
        panel.add(scrollTable, BorderLayout.CENTER);

        // Panel du bas avec coût total et boutons
        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel coutPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        coutPanel.add(new JLabel("Coût total des réparations validées : "));
        lblCoutTotal = new JLabel("0.00 Ar");
        lblCoutTotal.setFont(new Font("Arial", Font.BOLD, 14));
        lblCoutTotal.setForeground(new Color(0, 100, 150));
        coutPanel.add(lblCoutTotal);
        bottomPanel.add(coutPanel, BorderLayout.WEST);

        JPanel btnBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton btnRecapitulatifTri = new JButton("📊 Récapitulatif avec Tri");
        btnRecapitulatifTri.setBackground(new Color(70, 130, 180));
        btnRecapitulatifTri.setForeground(Color.WHITE);
        btnRecapitulatifTri.setFont(new Font("Arial", Font.BOLD, 12));
        btnRecapitulatifTri.addActionListener(e -> afficherRecapitulatif());
        btnBottomPanel.add(btnRecapitulatifTri);

        JButton btnSupprimer = new JButton("Supprimer Sélectionnée");
        btnSupprimer.addActionListener(e -> supprimerReparationDegat());
        btnBottomPanel.add(btnSupprimer);

        JButton btnToggleValidation = new JButton("Changer Validation");
        btnToggleValidation.addActionListener(e -> changerValidationReparation());
        btnBottomPanel.add(btnToggleValidation);

        bottomPanel.add(btnBottomPanel, BorderLayout.EAST);
        panel.add(bottomPanel, BorderLayout.SOUTH);

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

            int id = degatDAO.inserer(degat);

            JOptionPane.showMessageDialog(this, "Dégât ajouté avec succès (ID: " + id + ")",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            txtPointKm.setText("");
            txtSurfaceM2.setText("");
            txtProfondeurM.setText("");

            chargerDegats(chemin.getId());
            rafraichirComboDegats(); // Rafraîchir les dropdowns

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

                modelDegats.addRow(new Object[] {
                        d.getId(),
                        cheminNom,
                        String.format("%.2f", d.getPointKm()),
                        String.format("%.2f", d.getSurfaceM2()),
                        String.format("%.2f", d.getProfondeurM())
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

    // ==================== NOUVELLES MÉTHODES ====================

    private void rafraichirComboDegats() {
        if (cmbDegatModifier != null) {
            cmbDegatModifier.removeAllItems();
        }
        if (cmbDegatReparation != null) {
            cmbDegatReparation.removeAllItems();
        }

        try {
            List<Degat> degats = degatDAO.getAll();
            for (Degat d : degats) {
                if (cmbDegatModifier != null) {
                    cmbDegatModifier.addItem(d);
                }
                if (cmbDegatReparation != null) {
                    cmbDegatReparation.addItem(d);
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des dégâts: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void rafraichirComboMateriauxReparation() {
        if (cmbMateriauReparation == null)
            return;
        cmbMateriauReparation.removeAllItems();
        try {
            List<Materiau> materiaux = materiauDAO.getAll();
            for (Materiau m : materiaux) {
                cmbMateriauReparation.addItem(m);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement des matériaux: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modifierPointDegat() {
        Degat degat = (Degat) cmbDegatModifier.getSelectedItem();
        if (degat == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un dégât",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double nouveauPoint = Double.parseDouble(txtNouveauPointKm.getText().trim());
            if (nouveauPoint < 0) {
                JOptionPane.showMessageDialog(this, "Le point kilométrique doit être >= 0",
                        "Erreur", JOptionPane.ERROR_MESSAGE);
                return;
            }

            degatDAO.mettreAJourPointKm(degat.getId(), nouveauPoint);
            JOptionPane.showMessageDialog(this, "Point kilométrique modifié avec succès",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            txtNouveauPointKm.setText("");
            rafraichirComboDegats();
            chargerDegats(null); // Recharger tous les dégâts

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Veuillez saisir une valeur numérique valide",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void associerReparation() {
        Degat degat = (Degat) cmbDegatReparation.getSelectedItem();
        Materiau materiau = (Materiau) cmbMateriauReparation.getSelectedItem();

        if (degat == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un dégât",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (materiau == null) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner un matériau",
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ReparationDegat rd = new ReparationDegat();
            rd.setDegatId(degat.getId());
            rd.setMateriauId(materiau.getId());
            rd.setValidee(false);

            int id = reparationDegatDAO.inserer(rd);
            JOptionPane.showMessageDialog(this, "Réparation associée avec succès (ID: " + id + ")",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            chargerReparationsDegats();
            mettreAJourCoutTotal();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerReparationsDegats() {
        modelReparationsDegats.setRowCount(0);
        try {
            List<ReparationDegat> reparations = reparationDegatDAO.getAll();
            for (ReparationDegat rd : reparations) {
                modelReparationsDegats.addRow(new Object[] {
                        rd.getId(),
                        rd.getDegatInfo(),
                        rd.getMateriauNom(),
                        String.format("%.2f", rd.getCoutReparation()),
                        rd.isValidee() ? "✓ Oui" : "✗ Non"
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors du chargement: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void supprimerReparationDegat() {
        int selectedRow = tableReparationsDegats.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelReparationsDegats.getValueAt(selectedRow, 0);
        String degatInfo = (String) modelReparationsDegats.getValueAt(selectedRow, 1);

        int confirmation = JOptionPane.showConfirmDialog(this,
                "Voulez-vous vraiment supprimer cette réparation ?\n" + degatInfo,
                "Confirmation", JOptionPane.YES_NO_OPTION);

        if (confirmation == JOptionPane.YES_OPTION) {
            try {
                reparationDegatDAO.supprimer(id);
                JOptionPane.showMessageDialog(this, "Réparation supprimée avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
                chargerReparationsDegats();
                mettreAJourCoutTotal();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void changerValidationReparation() {
        int selectedRow = tableReparationsDegats.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Veuillez sélectionner une réparation",
                    "Attention", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) modelReparationsDegats.getValueAt(selectedRow, 0);
        String valideeStr = (String) modelReparationsDegats.getValueAt(selectedRow, 4);
        boolean estValidee = valideeStr.contains("✓");

        try {
            if (estValidee) {
                reparationDegatDAO.annulerValidation(id);
                JOptionPane.showMessageDialog(this, "Validation annulée",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } else {
                reparationDegatDAO.valider(id);
                JOptionPane.showMessageDialog(this, "Réparation validée",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

            chargerReparationsDegats();
            mettreAJourCoutTotal();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void validerToutesReparations() {
        try {
            List<ReparationDegat> reparations = reparationDegatDAO.getAll();
            for (ReparationDegat rd : reparations) {
                if (!rd.isValidee()) {
                    reparationDegatDAO.valider(rd.getId());
                }
            }

            JOptionPane.showMessageDialog(this, "Toutes les réparations ont été validées",
                    "Succès", JOptionPane.INFORMATION_MESSAGE);

            chargerReparationsDegats();
            mettreAJourCoutTotal();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mettreAJourCoutTotal() {
        try {
            double total = reparationDegatDAO.calculerCoutTotalValidees();
            lblCoutTotal.setText(String.format("%.2f Ar", total));
        } catch (SQLException ex) {
            lblCoutTotal.setText("Erreur");
        }
    }

    private void afficherRecapitulatif() {
        try {
            List<ReparationDegat> reparationsValidees = reparationDegatDAO.getValidees();

            if (reparationsValidees.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Aucune réparation validée à afficher",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Créer une nouvelle fenêtre pour le récapitulatif
            JFrame frameRecap = new JFrame("Récapitulatif des Réparations Validées");
            frameRecap.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frameRecap.setLayout(new BorderLayout());

            // Table récapitulative
            String[] colonnes = { "Dégât", "Matériau", "Coût (Ar)" };
            DefaultTableModel modelRecap = new DefaultTableModel(colonnes, 0);

            // Méthode pour remplir le tableau
            Runnable remplirTableau = () -> {
                modelRecap.setRowCount(0);
                for (ReparationDegat rd : reparationsValidees) {
                    modelRecap.addRow(new Object[] {
                            rd.getDegatInfo(),
                            rd.getMateriauNom(),
                            String.format("%.2f", rd.getCoutReparation())
                    });
                }
            };

            // Remplissage initial
            remplirTableau.run();

            JTable tableRecap = new JTable(modelRecap);
            tableRecap.getColumnModel().getColumn(0).setPreferredWidth(300);
            tableRecap.getColumnModel().getColumn(1).setPreferredWidth(120);
            tableRecap.getColumnModel().getColumn(2).setPreferredWidth(100);

            // Panel de tri
            JPanel panelTri = new JPanel(new FlowLayout(FlowLayout.CENTER));
            panelTri.setBorder(BorderFactory.createTitledBorder("Tri par Coût"));
            panelTri.setBackground(new Color(240, 240, 240));

            JButton btnTriAscendant = new JButton("↑ Croissant");
            btnTriAscendant.setPreferredSize(new Dimension(120, 30));
            btnTriAscendant.addActionListener(e -> {
                reparationsValidees.sort((r1, r2) -> Double.compare(r1.getCoutReparation(), r2.getCoutReparation()));
                remplirTableau.run();
                modelRecap.fireTableDataChanged(); // Notifier le changement
            });

            JButton btnTriDescendant = new JButton("↓ Décroissant");
            btnTriDescendant.setPreferredSize(new Dimension(120, 30));
            btnTriDescendant.addActionListener(e -> {
                reparationsValidees.sort((r1, r2) -> Double.compare(r2.getCoutReparation(), r1.getCoutReparation()));
                remplirTableau.run();
                modelRecap.fireTableDataChanged(); // Notifier le changement
            });

            panelTri.add(new JLabel("Trier par coût : "));
            panelTri.add(btnTriAscendant);
            panelTri.add(btnTriDescendant);

            // Forcer une hauteur minimale pour le panel de tri
            panelTri.setPreferredSize(new Dimension(600, 80));
            // Panel central avec table et boutons de tri
            JPanel panelCentral = new JPanel(new BorderLayout());
            panelCentral.add(panelTri, BorderLayout.NORTH);
            panelCentral.add(new JScrollPane(tableRecap), BorderLayout.CENTER);

            frameRecap.add(panelCentral, BorderLayout.CENTER);

            // Panel total
            double totalGeneral = reparationsValidees.stream().mapToDouble(ReparationDegat::getCoutReparation).sum();
            JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JLabel lblTotalRecap = new JLabel("TOTAL GÉNÉRAL : " + String.format("%.2f Ar", totalGeneral));
            lblTotalRecap.setFont(new Font("Arial", Font.BOLD, 16));
            lblTotalRecap.setForeground(new Color(150, 0, 0));
            panelTotal.add(lblTotalRecap);

            frameRecap.add(panelTotal, BorderLayout.SOUTH);

            frameRecap.setSize(650, 450);
            frameRecap.setLocationRelativeTo(this);
            frameRecap.setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erreur: " + ex.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }
}
