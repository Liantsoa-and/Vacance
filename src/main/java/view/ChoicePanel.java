package view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;
import model.ResultatTrajet;
import controller.TrajetController;

public class ChoicePanel extends JPanel {
    private JTable tableChemins;
    private JTextArea detailsArea;
    private DefaultTableModel tableModel;
    private List<ResultatTrajet> resultatsActuels;

    public ChoicePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Chemins Disponibles"));

        // Table des chemins - Ajout de colonnes pour les horaires
        tableModel = new DefaultTableModel(
                new String[] { "Classement", "Distance (km)", "Vitesse Moyenne Réelle", "Temps Route", "Attente",
                        "Total", "Arrivée", "Coût Réparation (Ar)", "Conforme" },
                0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tableChemins = new JTable(tableModel);
        tableChemins.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableChemins.setRowHeight(25);
        tableChemins.getSelectionModel().addListSelectionListener(e -> afficherDetails());

        // Personnaliser le rendu des cellules
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < tableChemins.getColumnCount(); i++) {
            tableChemins.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Renderer pour la colonne "Conforme"
        tableChemins.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if ("Oui".equals(value)) {
                    c.setBackground(isSelected ? table.getSelectionBackground() : new Color(200, 255, 200));
                    c.setForeground(isSelected ? table.getSelectionForeground() : new Color(0, 100, 0));
                } else {
                    c.setBackground(isSelected ? table.getSelectionBackground() : new Color(255, 200, 200));
                    c.setForeground(isSelected ? table.getSelectionForeground() : new Color(150, 0, 0));
                }

                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });

        JScrollPane scrollTable = new JScrollPane(tableChemins);
        add(scrollTable, BorderLayout.CENTER);

        // Panel des détails
        JPanel panelDetails = new JPanel(new BorderLayout(10, 10));
        panelDetails.setBorder(BorderFactory.createTitledBorder("Détails du Chemin Sélectionné"));

        detailsArea = new JTextArea(10, 30);
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollDetails = new JScrollPane(detailsArea);
        panelDetails.add(scrollDetails, BorderLayout.CENTER);

        add(panelDetails, BorderLayout.SOUTH);
    }

    public void afficherResultats(List<ResultatTrajet> resultats) {
        this.resultatsActuels = resultats;
        tableModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

        for (int i = 0; i < resultats.size(); i++) {
            ResultatTrajet rt = resultats.get(i);
            String tempsRoute = TrajetController.formatDuree(rt.getTempsTotalHeures());
            String tempsAttente = rt.getTempsAttenteTotalHeures() > 0
                    ? TrajetController.formatDuree(rt.getTempsAttenteTotalHeures())
                    : "-";
            String tempsTotal = TrajetController.formatDuree(rt.getTempsTotalAvecPausesHeures());
            String heureArrivee = rt.getHeureArrivee() != null ? sdf.format(rt.getHeureArrivee()) : "-";
            String conforme = rt.isConforme() ? "Oui" : "Non";
            String vitesseReelle = String.format("%.2f km/h", rt.getVitesseMoyenneReelle());
            String coutReparation = String.format("%.2f", rt.getCoutReparationAr());

            tableModel.addRow(new Object[] {
                    i + 1,
                    String.format("%.2f", rt.getDistanceTotaleKm()),
                    vitesseReelle,
                    tempsRoute,
                    tempsAttente,
                    tempsTotal,
                    heureArrivee,
                    coutReparation,
                    conforme
            });
        }

        if (resultats.size() > 0) {
            tableChemins.setRowSelectionInterval(0, 0);
        }
    }

    private void afficherDetails() {
        int selectedRow = tableChemins.getSelectedRow();
        if (selectedRow >= 0 && resultatsActuels != null && selectedRow < resultatsActuels.size()) {
            ResultatTrajet rt = resultatsActuels.get(selectedRow);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");

            detailsArea.setText("═══════════════════════════════════\n");
            detailsArea.append("   DÉTAILS DU TRAJET #" + (selectedRow + 1) + "\n");
            detailsArea.append("═══════════════════════════════════\n\n");

            detailsArea.append("📍 Distance      : " + String.format("%.2f km", rt.getDistanceTotaleKm()) + "\n");
            detailsArea.append("🚗 Vitesse réelle: " + String.format("%.2f km/h", rt.getVitesseMoyenneReelle())
                    + " (avec dommages)\n");
            detailsArea.append("💰 Coût réparation: " + String.format("%.2f Ar", rt.getCoutReparationAr()) + "\n");
            detailsArea.append("⏱️  Temps route   : " + rt.getTempsFormate() + "\n");

            if (rt.getTempsAttenteTotalHeures() > 0) {
                detailsArea.append("⏸️  Temps d'attente: " + rt.getTempsAttenteFormate() + "\n");
                detailsArea.append("⏰  Temps total   : " + rt.getTempsTotalAvecPausesFormate() + "\n");
            }

            if (rt.getHeureDepart() != null) {
                detailsArea.append("\n🚀 Départ        : " + sdf.format(rt.getHeureDepart()) + "\n");
            }
            if (rt.getHeureArrivee() != null) {
                detailsArea.append("🏁 Arrivée       : " + sdf.format(rt.getHeureArrivee()) + "\n");
            }

            detailsArea.append("\n✓  Conforme      : " + (rt.isConforme() ? "Oui" : "Non") + "\n");

            if (!rt.isConforme()) {
                detailsArea.append("\n⚠️  ATTENTION: ");
                if (!rt.peutFaireTrajetAvecPleinReservoir()) {
                    detailsArea.append("Carburant insuffisant!\n");
                } else {
                    detailsArea.append("Chemins trop étroits!\n");
                }
            }

            if (rt.getTempsAttenteTotalHeures() > 0) {
                detailsArea.append("\nℹ️  Ce trajet comporte des pauses\n");
                detailsArea.append("   obligatoires qui rallongent le temps.\n");
            }
        }
    }

    public int getCheminSelectionne() {
        return tableChemins.getSelectedRow();
    }

    public JTable getTableChemins() {
        return tableChemins;
    }
}