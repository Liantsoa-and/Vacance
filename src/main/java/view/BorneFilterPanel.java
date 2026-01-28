package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;
import dao.CheminDAO;
import dao.BorneDAO;
import model.Chemin;
import model.Borne;
import controller.BorneController;

/**
 * Panel pour filtrer et afficher le prix de réparation entre deux bornes
 * kilométriques
 */
public class BorneFilterPanel extends JPanel {

    private JComboBox<String> combChemin;
    private JTextField txtKmDebut;
    private JTextField txtKmFin;
    private JButton btnCalculer;
    private JButton btnAjouterBorne;
    private JTable tableDegats;
    private DefaultTableModel tableModel;
    private JLabel lblCoutTotal;
    private JLabel lblNombreDegats;
    private JLabel lblInfoBornes;

    private List<Chemin> chemins;
    private BorneController borneController;
    private CheminDAO cheminDAO;
    private BorneDAO borneDAO;

    public BorneFilterPanel() {
        borneController = new BorneController();
        cheminDAO = new CheminDAO();
        borneDAO = new BorneDAO();

        initComponents();
        chargerChemins();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel principal avec titre
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));

        // En-tête avec titre
        JLabel lblTitre = new JLabel("📍 Calcul des Réparations entre Bornes Kilométriques");
        lblTitre.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitre.setHorizontalAlignment(SwingConstants.CENTER);
        lblTitre.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelPrincipal.add(lblTitre, BorderLayout.NORTH);

        // Panel de sélection
        JPanel panelSelection = new JPanel(new GridLayout(4, 2, 10, 10));
        panelSelection.setBorder(BorderFactory.createTitledBorder("Sélection de la Zone"));

        // Chemin
        panelSelection.add(new JLabel("Chemin :"));
        combChemin = new JComboBox<>();
        panelSelection.add(combChemin);

        // Borne début (km)
        panelSelection.add(new JLabel("Borne Début (km) :"));
        txtKmDebut = new JTextField("0.0");
        panelSelection.add(txtKmDebut);

        // Borne fin (km)
        panelSelection.add(new JLabel("Borne Fin (km) :"));
        txtKmFin = new JTextField("10.0");
        panelSelection.add(txtKmFin);

        // Boutons
        JPanel panelBoutons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnCalculer = new JButton("🔍 Calculer");
        btnCalculer.setFont(new Font("Arial", Font.BOLD, 12));
        btnCalculer.setBackground(new Color(0, 120, 215));
        btnCalculer.setForeground(Color.WHITE);
        btnCalculer.addActionListener(e -> calculerCoutReparation());
        panelBoutons.add(btnCalculer);

        btnAjouterBorne = new JButton("⚙ Gérer les Bornes");
        btnAjouterBorne.addActionListener(e -> ouvrirGestionBornes());
        panelBoutons.add(btnAjouterBorne);

        panelSelection.add(new JLabel(""));
        panelSelection.add(panelBoutons);

        panelPrincipal.add(panelSelection, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.NORTH);

        // Panel de résultats
        JPanel panelResultats = new JPanel(new BorderLayout(10, 10));
        panelResultats.setBorder(BorderFactory.createTitledBorder("Résultats"));

        // Panel d'informations en haut
        JPanel panelInfo = new JPanel(new GridLayout(3, 1, 5, 5));
        panelInfo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblInfoBornes = new JLabel("Zone : - km → - km", SwingConstants.CENTER);
        lblInfoBornes.setFont(new Font("Arial", Font.PLAIN, 14));
        panelInfo.add(lblInfoBornes);

        lblNombreDegats = new JLabel("Nombre de dégâts : 0", SwingConstants.CENTER);
        lblNombreDegats.setFont(new Font("Arial", Font.BOLD, 14));
        panelInfo.add(lblNombreDegats);

        lblCoutTotal = new JLabel("Coût Total : 0.00 Ar", SwingConstants.CENTER);
        lblCoutTotal.setFont(new Font("Arial", Font.BOLD, 22));
        lblCoutTotal.setForeground(new Color(0, 100, 0));
        panelInfo.add(lblCoutTotal);

        panelResultats.add(panelInfo, BorderLayout.NORTH);

        // Table des dégâts
        String[] colonnes = { "N°", "Position (km)", "Surface (m²)", "Profondeur (m)", "Matériau", "Coût (Ar)",
                "Description" };
        tableModel = new DefaultTableModel(colonnes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableDegats = new JTable(tableModel);
        tableDegats.setFont(new Font("Monospaced", Font.PLAIN, 12));
        tableDegats.setRowHeight(25);
        tableDegats.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));

        // Personnaliser les colonnes
        tableDegats.getColumnModel().getColumn(0).setPreferredWidth(40); // N°
        tableDegats.getColumnModel().getColumn(1).setPreferredWidth(100); // Position
        tableDegats.getColumnModel().getColumn(2).setPreferredWidth(90); // Surface
        tableDegats.getColumnModel().getColumn(3).setPreferredWidth(110); // Profondeur
        tableDegats.getColumnModel().getColumn(4).setPreferredWidth(150); // Matériau
        tableDegats.getColumnModel().getColumn(5).setPreferredWidth(100); // Coût
        tableDegats.getColumnModel().getColumn(6).setPreferredWidth(70); // Validée

        JScrollPane scrollPane = new JScrollPane(tableDegats);
        panelResultats.add(scrollPane, BorderLayout.CENTER);

        add(panelResultats, BorderLayout.CENTER);
    }

    private void chargerChemins() {
        try {
            chemins = cheminDAO.getAll();
            combChemin.removeAllItems();
            for (Chemin chemin : chemins) {
                combChemin.addItem(chemin.getNom() + " (ID: " + chemin.getId() + ")");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du chargement des chemins : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calculerCoutReparation() {
        try {
            // Validation des champs
            if (combChemin.getSelectedIndex() == -1) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner un chemin",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            double kmDebut = Double.parseDouble(txtKmDebut.getText().trim());
            double kmFin = Double.parseDouble(txtKmFin.getText().trim());

            if (kmDebut >= kmFin) {
                JOptionPane.showMessageDialog(this,
                        "La borne de début doit être inférieure à la borne de fin",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Récupérer le chemin sélectionné
            Chemin cheminSelectionne = chemins.get(combChemin.getSelectedIndex());

            // Calculer les réparations avec sélection automatique du matériau selon les
            // précipitations
            BorneController.ResultatReparationBornes resultat = borneController.calculerReparationComplete(
                    cheminSelectionne.getId(),
                    kmDebut,
                    kmFin);

            // Mettre à jour les labels d'information
            lblInfoBornes.setText(String.format("Zone : %.2f km → %.2f km (Distance: %.2f km) - Chemin: %s",
                    kmDebut, kmFin, (kmFin - kmDebut), cheminSelectionne.getNom()));
            lblNombreDegats.setText(String.format("Nombre de dégâts trouvés : %d", resultat.getNombreDegats()));
            lblCoutTotal.setText(String.format("Coût Total : %.2f €", resultat.getCoutTotal()));

            // Remplir la table avec les dégâts
            tableModel.setRowCount(0); // Vider la table

            if (resultat.getNombreDegats() == 0) {
                JOptionPane.showMessageDialog(this,
                        "Aucun dégât trouvé dans cette zone.",
                        "Information", JOptionPane.INFORMATION_MESSAGE);
            } else {
                int numero = 1;

                for (BorneController.DegatAvecCout degatAvecCout : resultat.getDegatsAvecCout()) {
                    String materiauNom = degatAvecCout.getMateriau() != null
                            ? degatAvecCout.getMateriau().getNom()
                            : "Erreur matériau";

                    String cout = degatAvecCout.getCoutReparation() > 0
                            ? String.format("%.2f", degatAvecCout.getCoutReparation())
                            : "-";

                    String description = degatAvecCout.getDescriptionReparation() != null
                            ? degatAvecCout.getDescriptionReparation()
                            : "Auto";

                    tableModel.addRow(new Object[] {
                            numero++,
                            String.format("%.2f", degatAvecCout.getDegat().getPointKm()),
                            String.format("%.2f", degatAvecCout.getDegat().getSurfaceM2()),
                            String.format("%.2f", degatAvecCout.getDegat().getProfondeurM()),
                            materiauNom,
                            cout,
                            description
                    });
                }

                // Afficher un résumé
                String message = String.format(
                        "✓ Calcul terminé!\n\n" +
                                "Zone analysée : %.2f km à %.2f km\n" +
                                "Dégâts trouvés : %d\n" +
                                "Coût total des réparations : %.2f €\n\n" +
                                "Le matériau a été sélectionné automatiquement selon\n" +
                                "le niveau de précipitation à chaque position.\n\n" +
                                "Les détails sont affichés dans le tableau ci-dessous.",
                        kmDebut, kmFin, resultat.getNombreDegats(), resultat.getCoutTotal());

                JOptionPane.showMessageDialog(this, message,
                        "Calcul terminé", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez saisir des valeurs numériques valides pour les bornes kilométriques",
                    "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Erreur lors du calcul : " + e.getMessage(),
                    "Erreur", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void ouvrirGestionBornes() {
        if (combChemin.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this,
                    "Veuillez sélectionner un chemin",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Chemin cheminSelectionne = chemins.get(combChemin.getSelectedIndex());

        // Créer une fenêtre de dialogue pour gérer les bornes
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Gestion des Bornes - " + cheminSelectionne.getNom(), true);
        dialog.setLayout(new BorderLayout());

        BorneManagementPanel bornePanel = new BorneManagementPanel(cheminSelectionne.getId());
        dialog.add(bornePanel, BorderLayout.CENTER);

        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    /**
     * Panel interne pour gérer les bornes d'un chemin
     */
    private class BorneManagementPanel extends JPanel {
        private int cheminId;
        private JList<String> listBornes;
        private DefaultListModel<String> listModel;
        private JTextField txtNouvelleKm;
        private List<Borne> bornes;

        public BorneManagementPanel(int cheminId) {
            this.cheminId = cheminId;
            initComponents();
            chargerBornes();
        }

        private void initComponents() {
            setLayout(new BorderLayout(10, 10));
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Liste des bornes
            listModel = new DefaultListModel<>();
            listBornes = new JList<>(listModel);
            JScrollPane scrollPane = new JScrollPane(listBornes);
            add(scrollPane, BorderLayout.CENTER);

            // Panel d'ajout
            JPanel panelAjout = new JPanel(new FlowLayout());
            panelAjout.add(new JLabel("Nouvelle borne (km) :"));
            txtNouvelleKm = new JTextField(10);
            panelAjout.add(txtNouvelleKm);

            JButton btnAjouter = new JButton("Ajouter");
            btnAjouter.addActionListener(e -> ajouterBorne());
            panelAjout.add(btnAjouter);

            JButton btnSupprimer = new JButton("Supprimer");
            btnSupprimer.addActionListener(e -> supprimerBorne());
            panelAjout.add(btnSupprimer);

            add(panelAjout, BorderLayout.SOUTH);
        }

        private void chargerBornes() {
            try {
                bornes = borneDAO.getBornesByCheminId(cheminId);
                listModel.clear();
                for (Borne borne : bornes) {
                    listModel.addElement(String.format("Borne %.2f km (ID: %d)", borne.getKm(), borne.getId()));
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors du chargement des bornes : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void ajouterBorne() {
            try {
                double km = Double.parseDouble(txtNouvelleKm.getText().trim());

                Borne nouvelleBorne = new Borne(0, cheminId, km);
                borneDAO.insert(nouvelleBorne);

                txtNouvelleKm.setText("");
                chargerBornes();

                JOptionPane.showMessageDialog(this,
                        "Borne ajoutée avec succès",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez saisir une valeur numérique valide",
                        "Erreur de saisie", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de l'ajout : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }

        private void supprimerBorne() {
            int selectedIndex = listBornes.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(this,
                        "Veuillez sélectionner une borne à supprimer",
                        "Validation", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                Borne borneASupprimer = bornes.get(selectedIndex);
                int confirm = JOptionPane.showConfirmDialog(this,
                        String.format("Supprimer la borne %.2f km ?", borneASupprimer.getKm()),
                        "Confirmation", JOptionPane.YES_NO_OPTION);

                if (confirm == JOptionPane.YES_OPTION) {
                    borneDAO.delete(borneASupprimer.getId());
                    chargerBornes();

                    JOptionPane.showMessageDialog(this,
                            "Borne supprimée avec succès",
                            "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Erreur lors de la suppression : " + e.getMessage(),
                        "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
