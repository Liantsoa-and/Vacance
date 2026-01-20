package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import dao.VehiculeDAO;
import dao.PointDAO;
import model.Vehicule;
import model.Point;

public class SelectionPanel extends JPanel {
    private JComboBox<String> combVehicules;
    private JComboBox<String> combPointDepart;
    private JComboBox<String> combPointArrivee;
    private JTextField inputVitesse;
    private JSpinner spinnerHeure;
    private JButton btnCalculer;

    private List<Vehicule> vehicules;
    private List<Point> points;

    public SelectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Sélection du Trajet"));

        vehicules = new java.util.ArrayList<>();
        points = new java.util.ArrayList<>();

        chargerVehicules();
        chargerPoints();

        JPanel panelChamps = new JPanel(new GridLayout(5, 2, 10, 10));
        panelChamps.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Véhicule
        panelChamps.add(new JLabel("Véhicule :"));
        combVehicules = new JComboBox<>();
        for (Vehicule v : vehicules) {
            combVehicules.addItem(v.getNom());
        }
        panelChamps.add(combVehicules);

        // Point de départ
        panelChamps.add(new JLabel("Point de Départ :"));
        combPointDepart = new JComboBox<>();
        for (Point p : points) {
            combPointDepart.addItem(p.getNom());
        }
        panelChamps.add(combPointDepart);

        // Point d'arrivée
        panelChamps.add(new JLabel("Point d'Arrivée :"));
        combPointArrivee = new JComboBox<>();
        for (Point p : points) {
            combPointArrivee.addItem(p.getNom());
        }
        panelChamps.add(combPointArrivee);

        // Vitesse moyenne
        panelChamps.add(new JLabel("Vitesse Moyenne (km/h) :"));
        inputVitesse = new JTextField("80");
        panelChamps.add(inputVitesse);

        // Heure de départ
        panelChamps.add(new JLabel("Heure de Départ :"));
        SpinnerDateModel model = new SpinnerDateModel();
        spinnerHeure = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerHeure, "HH:mm");
        spinnerHeure.setEditor(editor);
        spinnerHeure.setValue(new Date()); // Heure actuelle par défaut
        panelChamps.add(spinnerHeure);

        add(panelChamps, BorderLayout.CENTER);

        // Panel bouton
        JPanel panelBouton = new JPanel();
        btnCalculer = new JButton("Calculer le Trajet");
        panelBouton.add(btnCalculer);
        add(panelBouton, BorderLayout.SOUTH);
    }

    private void chargerVehicules() {
        try {
            VehiculeDAO dao = new VehiculeDAO();
            vehicules = dao.getAll();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des véhicules : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void chargerPoints() {
        try {
            PointDAO dao = new PointDAO();
            points = dao.getAll();
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des points : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public int getIdVehiculeSelecte() {
        if (combVehicules.getSelectedIndex() >= 0) {
            return vehicules.get(combVehicules.getSelectedIndex()).getId();
        }
        return -1;
    }

    public int getIdPointDepart() {
        if (combPointDepart.getSelectedIndex() >= 0) {
            return points.get(combPointDepart.getSelectedIndex()).getId();
        }
        return -1;
    }

    public int getIdPointArrivee() {
        if (combPointArrivee.getSelectedIndex() >= 0) {
            return points.get(combPointArrivee.getSelectedIndex()).getId();
        }
        return -1;
    }

    public double getVitesseMoyenne() {
        try {
            double vitesse = Double.parseDouble(inputVitesse.getText());

            if (vitesse <= 0) {
                JOptionPane.showMessageDialog(this, "La vitesse doit être positive", "Erreur",
                        JOptionPane.ERROR_MESSAGE);
                return 0;
            }

            Vehicule vehiculeSelecte = getVehiculeSelecte();
            if (vehiculeSelecte != null && vitesse > vehiculeSelecte.getVitesseMax()) {
                JOptionPane.showMessageDialog(this,
                        "La vitesse moyenne (" + String.format("%.1f", vitesse) + " km/h) ne peut pas dépasser\n" +
                                "la vitesse maximale du véhicule ("
                                + String.format("%.1f", vehiculeSelecte.getVitesseMax()) + " km/h)",
                        "Vitesse invalide",
                        JOptionPane.WARNING_MESSAGE);
                return 0;
            }

            return vitesse;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Vitesse invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            return 0;
        }
    }

    public Date getHeureDepart() {
        return (Date) spinnerHeure.getValue();
    }

    public void ajouterEcoutantBouton(ActionListener listener) {
        btnCalculer.addActionListener(listener);
    }

    public Vehicule getVehiculeSelecte() {
        if (combVehicules.getSelectedIndex() >= 0) {
            return vehicules.get(combVehicules.getSelectedIndex());
        }
        return null;
    }
}