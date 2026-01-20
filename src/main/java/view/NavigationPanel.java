package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;
import model.Chemin;
import model.Point;
import model.ResultatTrajet;
import model.Dommage;
import model.Pause;
import dao.PointDAO;
import dao.DommageDAO;
import dao.PauseDAO;
import controller.TrajetController;

public class NavigationPanel extends JPanel {
    private ResultatTrajet trajet;
    private List<Point> pointsRoute;
    private int etapeCourante = 0;
    private int etapeDepart = 0; // Point de départ fixé à 0
    private double progression = 0.0; // 0.0 à 1.0 pour l'animation
    private double positionKmDansSegment = 0.0; // Position en km dans le segment actuel
    private Timer animationTimer;
    private boolean navigationActive = false;

    // Gestion des pauses obligatoires
    private long tempsArretPause = 0; // Temps d'arrêt en millisecondes (5 secondes = 5000 ms)
    private long tempsArretDebut = 0; // Timestamp de début d'arrêt
    private boolean enArretPause = false; // True si le véhicule est arrêté à une pause

    // Informations visuelles
    private JLabel lblEtape;
    private JLabel lblCheminActuel;
    private JLabel lblDistance;
    private JLabel lblVitesseActuelle;
    private JProgressBar progressBar;
    private JButton btnPlay;
    private JButton btnPause;
    private JButton btnReset;
    private JButton btnSpeedUp;
    private JButton btnSlowDown;

    private int vitesseAnimation = 16; // millisecondes entre frames (60 FPS pour fluidité)
    private double facteurVitesse = 1.0; // Multiplicateur de vitesse (modifiable par l'utilisateur)
    private double multiplicateurAnimation = 5000.0; // Multiplicateur pour rendre l'animation plus rapide visuellement
    private double vitesseMoyenneKmH; // Vitesse moyenne du véhicule en km/h

    public NavigationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createTitledBorder("Navigation Visuelle"));

        // Panel de dessin
        CanvasNavigation canvas = new CanvasNavigation();
        canvas.setPreferredSize(new Dimension(800, 500));
        add(canvas, BorderLayout.CENTER);

        // Panel de contrôles
        JPanel panelControles = createControlesPanel();
        add(panelControles, BorderLayout.SOUTH);

        // Panel d'informations
        JPanel panelInfo = createInfoPanel();
        add(panelInfo, BorderLayout.NORTH);
    }

    /**
     * Permet de modifier le multiplicateur d'animation pour ajuster la vitesse
     * visuelle
     * 
     * @param multiplicateur Valeur entre 1 (très lent) et 500 (très rapide).
     *                       Défaut: 50
     */
    public void setMultiplicateurAnimation(double multiplicateur) {
        this.multiplicateurAnimation = Math.max(1.0, Math.min(500.0, multiplicateur));
        System.out.println("Multiplicateur d'animation: x" + this.multiplicateurAnimation);
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Valeurs en dur (pas de choix utilisateur)
        etapeDepart = 0;
        multiplicateurAnimation = 5000.0; // Vitesse d'animation fixée

        // Infos principales
        JPanel panelInfos = new JPanel(new GridLayout(4, 2, 10, 5));

        panelInfos.add(new JLabel("📍 Étape :"));
        lblEtape = new JLabel("0 / 0");
        lblEtape.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfos.add(lblEtape);

        panelInfos.add(new JLabel("🛣️ Chemin actuel :"));
        lblCheminActuel = new JLabel("-");
        lblCheminActuel.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfos.add(lblCheminActuel);

        panelInfos.add(new JLabel("📏 Distance parcourue :"));
        lblDistance = new JLabel("0.00 / 0.00 km");
        lblDistance.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfos.add(lblDistance);

        panelInfos.add(new JLabel("🚗 Vitesse actuelle :"));
        lblVitesseActuelle = new JLabel("0 km/h");
        lblVitesseActuelle.setFont(new Font("Arial", Font.BOLD, 12));
        panelInfos.add(lblVitesseActuelle);

        // Barre de progression
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(400, 30));

        JPanel progressPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        progressPanel.add(new JLabel("Progression globale : "));
        progressPanel.add(progressBar);

        JPanel panelCentre = new JPanel(new BorderLayout(5, 5));
        panelCentre.add(panelInfos, BorderLayout.CENTER);

        panel.add(panelCentre, BorderLayout.CENTER);
        panel.add(progressPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createControlesPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        btnPlay = new JButton("▶ Démarrer");
        btnPause = new JButton("⏸ Pause");
        btnReset = new JButton("⏹ Reset");
        btnSlowDown = new JButton("🐌 Ralentir");
        btnSpeedUp = new JButton("🐇 Accélérer");

        // Style des boutons
        btnPlay.setBackground(new Color(0, 150, 0));
        btnPlay.setForeground(Color.WHITE);
        btnPlay.setFont(new Font("Arial", Font.BOLD, 12));

        btnPause.setBackground(new Color(200, 100, 0));
        btnPause.setForeground(Color.WHITE);
        btnPause.setFont(new Font("Arial", Font.BOLD, 12));
        btnPause.setEnabled(false);

        btnReset.setBackground(new Color(150, 0, 0));
        btnReset.setForeground(Color.WHITE);
        btnReset.setFont(new Font("Arial", Font.BOLD, 12));

        btnPlay.addActionListener(e -> demarrerNavigation());
        btnPause.addActionListener(e -> pauserNavigation());
        btnReset.addActionListener(e -> resetNavigation());
        btnSlowDown.addActionListener(e -> ralentirAnimation());
        btnSpeedUp.addActionListener(e -> accelererAnimation());

        panel.add(btnPlay);
        panel.add(btnPause);
        panel.add(btnReset);
        panel.add(btnSlowDown);
        panel.add(btnSpeedUp);

        // Ajouter un label pour afficher le multiplicateur actuel
        JLabel lblMultiplicateur = new JLabel("Vitesse: x" + String.format("%.1f", facteurVitesse));
        lblMultiplicateur.setFont(new Font("Arial", Font.BOLD, 11));
        panel.add(lblMultiplicateur);

        // Mettre à jour le label quand on change la vitesse
        btnSpeedUp.addActionListener(e -> {
            lblMultiplicateur.setText("Vitesse: x" + String.format("%.1f", facteurVitesse));
        });
        btnSlowDown.addActionListener(e -> {
            lblMultiplicateur.setText("Vitesse: x" + String.format("%.1f", facteurVitesse));
        });

        return panel;
    }

    public void chargerTrajet(ResultatTrajet trajet, double vitesseMoyenneKmH) {
        this.trajet = trajet;
        this.etapeCourante = 0;
        this.etapeDepart = 0;
        this.progression = 0.0;
        this.positionKmDansSegment = 0.0;
        this.vitesseMoyenneKmH = vitesseMoyenneKmH;

        System.out.println("=== Chargement du trajet ===");
        System.out.println("Vitesse moyenne configurée: " + vitesseMoyenneKmH + " km/h");

        // Charger les points de la route en suivant l'ordre exact des chemins
        pointsRoute = new ArrayList<>();
        try {
            PointDAO pointDAO = new PointDAO();
            List<Chemin> itineraire = trajet.getItineraire();

            if (!itineraire.isEmpty()) {
                // Construire pointsRoute en parcourant les chemins dans l'ordre
                // et en ajoutant les points de début et fin dans le bon ordre

                // 1. Ajouter le point de départ (début du premier chemin)
                int idPointCourant = itineraire.get(0).getPointDebut();
                Point pointCourant = pointDAO.getById(idPointCourant);
                pointsRoute.add(
                        pointCourant != null ? pointCourant : new Point(idPointCourant, "Point " + idPointCourant));

                System.out.println("Point 0: " + pointsRoute.get(0).getNom());

                // 2. Suivre l'itinéraire et ajouter chaque point d'arrivée
                for (int idx = 0; idx < itineraire.size(); idx++) {
                    Chemin c = itineraire.get(idx);
                    int idPointArrivee = c.getPointFin();
                    Point pointArrivee = pointDAO.getById(idPointArrivee);
                    pointsRoute.add(
                            pointArrivee != null ? pointArrivee : new Point(idPointArrivee, "Point " + idPointArrivee));

                    System.out.println(
                            "Point " + (idx + 1) + " (après " + c.getNom() + "): " + pointsRoute.get(idx + 1).getNom());
                }
            }

            System.out.println("Nombre de chemins: " + itineraire.size());
            System.out.println("Nombre de points chargés: " + pointsRoute.size());
            System.out.println("Vitesse moyenne: " + vitesseMoyenneKmH + " km/h");

        } catch (Exception e) {
            System.err.println("Erreur lors du chargement du trajet: " + e.getMessage());
            e.printStackTrace();

            // Fallback en cas d'erreur
            List<Chemin> itineraire = trajet.getItineraire();
            if (!itineraire.isEmpty()) {
                pointsRoute.add(new Point(itineraire.get(0).getPointDebut(), "Départ"));
                for (Chemin c : itineraire) {
                    pointsRoute.add(new Point(c.getPointFin(), c.getNom()));
                }
            }
        }

        mettreAJourInfos();
        repaint();
    }

    private void demarrerNavigation() {
        if (trajet == null)
            return;

        navigationActive = true;
        btnPause.setEnabled(true);

        if (animationTimer == null) {
            animationTimer = new Timer(vitesseAnimation, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    avancerAnimation();
                }
            });
        }
        animationTimer.start();
    }

    private void pauserNavigation() {
        navigationActive = false;
        btnPause.setEnabled(false);

        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    private void resetNavigation() {
        pauserNavigation();
        etapeDepart = 0;
        etapeCourante = etapeDepart; // Reset au point de départ fixé (0)
        progression = 0.0;
        positionKmDansSegment = 0.0;
        enArretPause = false;
        mettreAJourInfos();
        repaint();
    }

    private void avancerAnimation() {
        if (trajet == null || pointsRoute.size() < 2)
            return;

        if (etapeCourante >= trajet.getItineraire().size()) {
            pauserNavigation();
            JOptionPane.showMessageDialog(this,
                    "🎉 Navigation terminée!\n\n" +
                            "Distance totale: " + String.format("%.2f km", trajet.getDistanceTotaleKm()) + "\n" +
                            "Temps total: " + TrajetController.formatDuree(trajet.getTempsTotalHeures()),
                    "Arrivée à destination", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        Chemin cheminActuel = trajet.getItineraire().get(etapeCourante);

        // Vérifier si on est en arrêt à une pause obligatoire
        if (enArretPause) {
            long tempsEcoule = System.currentTimeMillis() - tempsArretDebut;
            if (tempsEcoule < tempsArretPause) {
                // Toujours en attente
                mettreAJourInfos();
                repaint();
                return;
            } else {
                // Arrêt terminé
                enArretPause = false;
                tempsArretPause = 0;
            }
        }

        // Calculer la vitesse actuelle en fonction de la position sur le segment
        double vitesseActuelleKmH = calculerVitesseActuelle(cheminActuel, positionKmDansSegment);

        // Convertir le délai d'animation (ms) en heures
        double deltaTempsHeures = (vitesseAnimation / 1000.0) / 3600.0;

        // Distance parcourue pendant cette frame = vitesse × temps × facteur
        // utilisateur × multiplicateur d'animation
        double distanceParcourueKm = vitesseActuelleKmH * deltaTempsHeures * facteurVitesse * multiplicateurAnimation;

        // Avancer dans le segment
        positionKmDansSegment += distanceParcourueKm;

        // Vérifier si on rencontre une pause obligatoire
        verifierPauseObligatoire(cheminActuel);

        // Calculer la progression (0-1) dans le segment
        progression = positionKmDansSegment / cheminActuel.getDistance();

        // Vérifier si on a terminé ce segment
        if (progression >= 1.0) {
            progression = 0.0;
            positionKmDansSegment = 0.0;
            etapeCourante++;

            if (etapeCourante >= trajet.getItineraire().size()) {
                pauserNavigation();
                JOptionPane.showMessageDialog(this,
                        "🎉 Navigation terminée!\n\n" +
                                "Distance totale: " + String.format("%.2f km", trajet.getDistanceTotaleKm()) + "\n" +
                                "Temps total: " + TrajetController.formatDuree(trajet.getTempsTotalHeures()),
                        "Arrivée à destination", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }

        mettreAJourInfos();
        repaint();
    }

    /**
     * Vérifie s'il y a une pause obligatoire à la position actuelle
     * Si oui et qu'on y arrive pendant le créneau, déclenche un arrêt de 5 secondes
     */
    private void verifierPauseObligatoire(Chemin chemin) {
        try {
            PauseDAO daoPause = new PauseDAO();
            List<Pause> pauses = daoPause.getByCheminId(chemin.getId());

            if (pauses == null || pauses.isEmpty()) {
                return;
            }

            // Adapter les positions si le chemin est parcouru en sens inverse
            int pointArriveePrec = (etapeCourante > 0) ? trajet.getItineraire().get(etapeCourante - 1).getPointFin()
                    : chemin.getPointDebut();
            boolean estInverse = (pointArriveePrec == chemin.getPointFin());

            for (Pause p : pauses) {
                double positionPause = estInverse ? (chemin.getDistance() - p.getPointKm()) : p.getPointKm();

                // Vérifier si on est proche de la pause (à 0.1 km près)
                if (Math.abs(positionKmDansSegment - positionPause) < 0.1) {
                    // Vérifier si c'est le créneau de la pause
                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    cal.setTime(new java.util.Date());
                    int heureCourante = cal.get(java.util.Calendar.HOUR_OF_DAY);
                    int minuteCourante = cal.get(java.util.Calendar.MINUTE);
                    int secondeCourante = cal.get(java.util.Calendar.SECOND);
                    long secsCourantes = heureCourante * 3600 + minuteCourante * 60 + secondeCourante;

                    cal.setTime(p.getHeureDebut());
                    int hDebut = cal.get(java.util.Calendar.HOUR_OF_DAY);
                    int mDebut = cal.get(java.util.Calendar.MINUTE);
                    int sDebut = cal.get(java.util.Calendar.SECOND);
                    long secsDebut = hDebut * 3600 + mDebut * 60 + sDebut;

                    cal.setTime(p.getHeureFin());
                    int hFin = cal.get(java.util.Calendar.HOUR_OF_DAY);
                    int mFin = cal.get(java.util.Calendar.MINUTE);
                    int sFin = cal.get(java.util.Calendar.SECOND);
                    long secsFin = hFin * 3600 + mFin * 60 + sFin;

                    // Si on arrive PENDANT la pause obligatoire, arrêt de 5 secondes
                    if (secsCourantes >= secsDebut && secsCourantes < secsFin) {
                        if (!enArretPause) {
                            enArretPause = true;
                            tempsArretPause = 5000; // 5 secondes en millisecondes
                            tempsArretDebut = System.currentTimeMillis();
                            System.out.println(
                                    "⏸ Arrêt à la pause de " + p.getDureeHeures() + "h - 5 secondes d'attente");
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des pauses: " + e.getMessage());
        }
    }

    /**
     * Calcule la vitesse actuelle du véhicule en fonction de sa position dans le
     * segment
     * Prend en compte les dommages qui réduisent la vitesse
     */
    private double calculerVitesseActuelle(Chemin chemin, double positionKm) {
        double vitesseActuelle = vitesseMoyenneKmH;

        try {
            DommageDAO daoDommage = new DommageDAO();
            List<Dommage> dommages = daoDommage.getByCheminId(chemin.getId());

            // Vérifier si la position actuelle est dans une zone de dommage
            for (Dommage d : dommages) {
                if (positionKm >= d.getDebutKm() && positionKm <= d.getFinKm()) {
                    // Réduire la vitesse selon le taux de dommage
                    vitesseActuelle = vitesseMoyenneKmH * (1.0 - d.getTaux());
                    break; // On prend le premier dommage trouvé
                }
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de la vitesse: " + e.getMessage());
        }

        return vitesseActuelle;
    }

    private void mettreAJourInfos() {
        if (trajet == null) {
            lblEtape.setText("0 / 0");
            lblCheminActuel.setText("-");
            lblDistance.setText("0.00 / 0.00 km");
            lblVitesseActuelle.setText("0 km/h");
            progressBar.setValue(0);
            return;
        }

        int totalEtapes = trajet.getItineraire().size();
        lblEtape.setText((etapeCourante + 1) + " / " + totalEtapes);

        // Nom du chemin actuel
        if (etapeCourante < totalEtapes) {
            Chemin cheminActuel = trajet.getItineraire().get(etapeCourante);
            lblCheminActuel.setText(cheminActuel.getNom());

            // Calculer et afficher la vitesse actuelle
            double vitesseActuelle = calculerVitesseActuelle(cheminActuel, positionKmDansSegment);
            lblVitesseActuelle.setText(String.format("%.1f km/h", vitesseActuelle));

            // Changer la couleur selon la vitesse
            if (vitesseActuelle < vitesseMoyenneKmH * 0.7) {
                lblVitesseActuelle.setForeground(Color.RED); // Très ralenti
            } else if (vitesseActuelle < vitesseMoyenneKmH * 0.9) {
                lblVitesseActuelle.setForeground(new Color(255, 140, 0)); // Ralenti
            } else {
                lblVitesseActuelle.setForeground(new Color(0, 150, 0)); // Vitesse normale
            }
        } else {
            lblCheminActuel.setText("Arrivé");
            lblVitesseActuelle.setText("0 km/h");
            lblVitesseActuelle.setForeground(Color.BLACK);
        }

        // Calculer la distance parcourue depuis le point de départ choisi
        double distanceParcourue = 0.0;
        for (int i = etapeDepart; i < etapeCourante; i++) {
            distanceParcourue += trajet.getItineraire().get(i).getDistance();
        }
        // Ajouter la distance parcourue dans le segment actuel
        if (etapeCourante < totalEtapes) {
            distanceParcourue += positionKmDansSegment;
        }

        // Calculer la distance totale depuis le point de départ choisi
        double distanceTotaleRestante = 0.0;
        for (int i = etapeDepart; i < totalEtapes; i++) {
            distanceTotaleRestante += trajet.getItineraire().get(i).getDistance();
        }

        lblDistance.setText(String.format("%.2f / %.2f km",
                distanceParcourue, distanceTotaleRestante));

        int progressionPourcent = distanceTotaleRestante > 0
                ? (int) ((distanceParcourue / distanceTotaleRestante) * 100)
                : 0;
        progressBar.setValue(progressionPourcent);
        progressBar.setString(progressionPourcent + "%");
    }

    private void accelererAnimation() {
        facteurVitesse = Math.min(5.0, facteurVitesse * 1.5);
        System.out.println("Vitesse d'animation accélérée: x" + String.format("%.1f", facteurVitesse));
    }

    private void ralentirAnimation() {
        facteurVitesse = Math.max(0.2, facteurVitesse / 1.5);
        System.out.println("Vitesse d'animation ralentie: x" + String.format("%.1f", facteurVitesse));
    }

    // Classe interne pour le dessin - Représentation linéaire/schématique
    class CanvasNavigation extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (trajet == null || pointsRoute.size() < 2) {
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                String message = "Aucun trajet chargé";
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(message)) / 2;
                g2d.setColor(Color.GRAY);
                g2d.drawString(message, x, getHeight() / 2);
                return;
            }

            // Dessiner la route de manière linéaire/schématique
            dessinerRouteSchemmatique(g2d);

            // Dessiner les détails du segment actuel
            dessinerDetailsSegment(g2d);

            // Dessiner la légende
            dessinerLegende(g2d);
        }

        private void dessinerRouteSchemmatique(Graphics2D g2d) {
            int margin = 80;
            int largeurUtile = getWidth() - 2 * margin;
            int yRoute = getHeight() / 3;

            List<Chemin> itineraire = trajet.getItineraire();

            // Vérification de sécurité
            if (pointsRoute == null || pointsRoute.isEmpty()) {
                g2d.setColor(Color.RED);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Erreur: Points non chargés", getWidth() / 2 - 80, getHeight() / 2);
                return;
            }

            // Calculer la distance totale pour proportionner les segments
            double distanceTotale = trajet.getDistanceTotaleKm();

            int xDebut = margin;
            double distanceCumulee = 0.0;
            // Dessiner chaque segment
            for (int i = 0; i < itineraire.size(); i++) {
                Chemin c = itineraire.get(i);
                double proportion = c.getDistance() / distanceTotale;
                int largeurSegment = (int) (largeurUtile * proportion);

                // Couleur selon l'état et le point de départ choisi
                Color couleur;
                if (i < etapeDepart) {
                    couleur = new Color(220, 220, 220); // Gris clair - avant le départ
                } else if (i < etapeCourante) {
                    couleur = new Color(50, 200, 50); // Vert - parcouru
                } else if (i == etapeCourante) {
                    couleur = new Color(255, 165, 0); // Orange - en cours
                } else {
                    couleur = new Color(180, 180, 180); // Gris - à venir
                }

                // Dessiner le segment
                g2d.setColor(couleur);
                g2d.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2d.drawLine(xDebut, yRoute, xDebut + largeurSegment, yRoute);

                // Vérifier s'il y a des dommages sur ce chemin et les afficher
                try {
                    DommageDAO daoDommage = new DommageDAO();
                    List<Dommage> dommages = daoDommage.getByCheminId(c.getId());

                    // Afficher chaque zone de dommage
                    for (Dommage d : dommages) {
                        double debutProportion = d.getDebutKm() / c.getDistance();
                        double finProportion = d.getFinKm() / c.getDistance();

                        int xDommageDebut = xDebut + (int) (largeurSegment * debutProportion);
                        int largeurDommage = (int) (largeurSegment * (finProportion - debutProportion));

                        // Couleur progressive selon le taux de dommage
                        Color couleurDommage;
                        if (d.getTaux() > 0.5) {
                            couleurDommage = new Color(255, 80, 80, 200); // Rouge intense
                        } else if (d.getTaux() > 0.3) {
                            couleurDommage = new Color(255, 150, 0, 180); // Orange vif
                        } else {
                            couleurDommage = new Color(255, 200, 0, 160); // Jaune doré
                        }

                        // Dessiner la zone de dommage en surbrillance
                        g2d.setColor(couleurDommage);
                        g2d.fillRect(xDommageDebut, yRoute - 28, largeurDommage, 56);

                        // Contour du dommage
                        g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                        g2d.setColor(new Color(200, 50, 50));
                        g2d.drawRect(xDommageDebut, yRoute - 28, largeurDommage, 56);
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de chargement des dommages
                }

                // Afficher les pauses sur ce chemin
                try {
                    PauseDAO daoPause = new PauseDAO();
                    List<Pause> pauses = daoPause.getByCheminId(c.getId());

                    if (pauses != null && !pauses.isEmpty()) {
                        // Adapter les positions si le chemin est parcouru en sens inverse
                        int pointArriveePrec = (i > 0) ? itineraire.get(i - 1).getPointFin() : c.getPointDebut();
                        boolean estInverse = (pointArriveePrec == c.getPointFin());

                        for (Pause pause : pauses) {
                            double positionPause = estInverse ? (c.getDistance() - pause.getPointKm())
                                    : pause.getPointKm();
                            double proportionPause = positionPause / c.getDistance();

                            int xPause = xDebut + (int) (largeurSegment * proportionPause);

                            // Dessiner un point violet pour la pause
                            g2d.setColor(new Color(150, 0, 200)); // Violet
                            g2d.fillOval(xPause - 8, yRoute - 8, 16, 16);

                            // Contour blanc
                            g2d.setColor(Color.WHITE);
                            g2d.setStroke(new BasicStroke(2));
                            g2d.drawOval(xPause - 8, yRoute - 8, 16, 16);

                            // Label "P" pour Pause
                            g2d.setColor(Color.WHITE);
                            g2d.setFont(new Font("Arial", Font.BOLD, 10));
                            g2d.drawString("P", xPause - 3, yRoute + 4);
                        }
                    }
                } catch (Exception e) {
                    // Ignorer les erreurs de chargement des pauses
                }

                // Dessiner le nom du chemin (en petit)
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 9));
                String nomCourt = c.getNom().length() > 12 ? c.getNom().substring(0, 12) + "..." : c.getNom();
                g2d.drawString(nomCourt, xDebut + 5, yRoute + 35);

                // Afficher la distance
                g2d.setFont(new Font("Arial", Font.BOLD, 10));
                g2d.drawString(String.format("%.1f km", c.getDistance()), xDebut + 5, yRoute + 50);

                xDebut += largeurSegment;
            }

            // Dessiner les points (villes) basés sur l'ordre des chemins
            // Les points sont positionnés selon la distance cumulée du trajet
            double distanceCumuleePoints = 0.0;
            for (int i = 0; i < pointsRoute.size(); i++) {
                Point p = pointsRoute.get(i);

                // Position x du point basée sur la distance cumulée
                int xPoint = margin + (int) ((distanceCumuleePoints / distanceTotale) * largeurUtile);

                // Ajouter la distance du chemin correspondant pour le prochain point
                if (i < itineraire.size()) {
                    distanceCumuleePoints += itineraire.get(i).getDistance();
                }

                // Couleur du point selon sa position par rapport au point de départ
                Color couleurPoint;
                if (i == etapeDepart) {
                    couleurPoint = new Color(0, 150, 0); // Point de départ choisi - vert vif
                } else if (i == pointsRoute.size() - 1) {
                    couleurPoint = new Color(200, 0, 0); // Arrivée finale - rouge
                } else if (i < etapeDepart) {
                    couleurPoint = new Color(200, 200, 200); // Avant le départ - gris clair
                } else if (i <= etapeCourante) {
                    couleurPoint = new Color(50, 100, 200); // Visité - bleu
                } else {
                    couleurPoint = new Color(150, 150, 150); // Non visité - gris
                }

                // Taille du point selon son statut
                int taillePoint = (i == etapeDepart) ? 28 : 24;
                int demiTaille = taillePoint / 2;

                // Dessiner le point
                g2d.setColor(couleurPoint);
                g2d.fillOval(xPoint - demiTaille, yRoute - demiTaille, taillePoint, taillePoint);
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(i == etapeDepart ? 3 : 2));
                g2d.drawOval(xPoint - demiTaille, yRoute - demiTaille, taillePoint, taillePoint);

                // Indicateur spécial pour le point de départ
                if (i == etapeDepart) {
                    g2d.setFont(new Font("Arial", Font.BOLD, 16));
                    g2d.drawString("🚩", xPoint - 8, yRoute - demiTaille - 5);
                }

                // Nom du point
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", i == etapeDepart ? Font.BOLD : Font.PLAIN, 12));
                FontMetrics fm = g2d.getFontMetrics();
                int largeurNom = fm.stringWidth(p.getNom());
                g2d.drawString(p.getNom(), xPoint - largeurNom / 2, yRoute - 20);
            }

            // Dessiner le véhicule (position actuelle)
            dessinerVehicule(g2d, margin, yRoute, largeurUtile, distanceTotale);
        }

        private void dessinerVehicule(Graphics2D g2d, int margin, int yRoute, int largeurUtile, double distanceTotale) {
            // Calculer la position du véhicule
            double distanceParcourue = 0.0;
            for (int i = 0; i < etapeCourante; i++) {
                distanceParcourue += trajet.getItineraire().get(i).getDistance();
            }
            if (etapeCourante < trajet.getItineraire().size()) {
                distanceParcourue += trajet.getItineraire().get(etapeCourante).getDistance() * progression;
            }

            int xVehicule = margin + (int) ((distanceParcourue / distanceTotale) * largeurUtile);

            // Dessiner un camion stylisé
            g2d.setColor(new Color(255, 100, 0));

            // Corps du camion
            int[] xPoints = { xVehicule - 15, xVehicule + 15, xVehicule + 15, xVehicule - 15 };
            int[] yPoints = { yRoute - 15, yRoute - 15, yRoute + 15, yRoute + 15 };
            g2d.fillPolygon(xPoints, yPoints, 4);

            // Cabine
            g2d.setColor(new Color(255, 150, 50));
            g2d.fillRect(xVehicule + 8, yRoute - 12, 12, 12);

            // Contour
            g2d.setColor(Color.BLACK);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawPolygon(xPoints, yPoints, 4);
            g2d.drawRect(xVehicule + 8, yRoute - 12, 12, 12);

            // Roues
            g2d.setColor(Color.BLACK);
            g2d.fillOval(xVehicule - 12, yRoute + 10, 10, 10);
            g2d.fillOval(xVehicule + 5, yRoute + 10, 10, 10);

            // Icône au-dessus
            g2d.setColor(new Color(255, 100, 0));
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            g2d.drawString("🚛", xVehicule - 10, yRoute - 25);
        }

        private void dessinerDetailsSegment(Graphics2D g2d) {
            if (etapeCourante >= trajet.getItineraire().size())
                return;

            Chemin cheminActuel = trajet.getItineraire().get(etapeCourante);

            int yDetails = (getHeight() * 2) / 3;
            int xDetails = 50;

            // Cadre
            g2d.setColor(new Color(240, 240, 255));
            g2d.fillRoundRect(xDetails, yDetails, getWidth() - 100, 150, 15, 15);
            g2d.setColor(new Color(100, 100, 200));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(xDetails, yDetails, getWidth() - 100, 150, 15, 15);

            // Titre
            g2d.setColor(new Color(50, 50, 150));
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString("📋 Segment actuel", xDetails + 15, yDetails + 25);

            // Détails
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 13));
            int y = yDetails + 50;
            g2d.drawString("Chemin : " + cheminActuel.getNom(), xDetails + 20, y);
            y += 22;
            g2d.drawString("Distance : " + String.format("%.2f km", cheminActuel.getDistance()), xDetails + 20, y);
            y += 22;

            // Afficher la vitesse actuelle avec code couleur
            double vitesseActuelle = calculerVitesseActuelle(cheminActuel, positionKmDansSegment);
            String texteVitesse = String.format("Vitesse : %.1f km/h", vitesseActuelle);

            if (vitesseActuelle < vitesseMoyenneKmH * 0.7) {
                g2d.setColor(Color.RED);
                texteVitesse += " ⚠️ RALENTISSEMENT";
            } else if (vitesseActuelle < vitesseMoyenneKmH * 0.9) {
                g2d.setColor(new Color(255, 140, 0));
                texteVitesse += " ⚠️";
            } else {
                g2d.setColor(new Color(0, 150, 0));
            }
            g2d.drawString(texteVitesse, xDetails + 20, y);

            // Barre de progression du segment avec zones de dommage
            int xBarre = xDetails + 20;
            int yBarre = yDetails + 100;
            int largeurBarre = getWidth() - 140;
            int hauteurBarre = 25;

            // Dessiner les zones de dommage en arrière-plan
            try {
                DommageDAO daoDommage = new DommageDAO();
                List<Dommage> dommages = daoDommage.getByCheminId(cheminActuel.getId());

                for (Dommage d : dommages) {
                    double debutProportion = d.getDebutKm() / cheminActuel.getDistance();
                    double finProportion = d.getFinKm() / cheminActuel.getDistance();

                    int xDebut = xBarre + (int) (largeurBarre * debutProportion);
                    int largeur = (int) (largeurBarre * (finProportion - debutProportion));

                    // Couleur selon le taux de réduction
                    Color couleurDommage;
                    if (d.getTaux() > 0.5) {
                        couleurDommage = new Color(255, 100, 100, 150); // Rouge pour gros dommage
                    } else if (d.getTaux() > 0.3) {
                        couleurDommage = new Color(255, 200, 100, 150); // Orange pour dommage moyen
                    } else {
                        couleurDommage = new Color(255, 255, 150, 150); // Jaune pour petit dommage
                    }

                    g2d.setColor(couleurDommage);
                    g2d.fillRect(xDebut, yBarre, largeur, hauteurBarre);
                }
            } catch (Exception e) {
                // Ignorer les erreurs
            }

            // Fond de la barre
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(xBarre, yBarre, largeurBarre, hauteurBarre);

            // Progression
            g2d.setColor(new Color(50, 150, 255));
            g2d.fillRect(xBarre, yBarre, (int) (largeurBarre * progression), hauteurBarre);

            // Contour
            g2d.setColor(Color.BLACK);
            g2d.drawRect(xBarre, yBarre, largeurBarre, hauteurBarre);

            // Texte de progression
            String progText = String.format("%.1f / %.1f km (%.0f%%)",
                    positionKmDansSegment,
                    cheminActuel.getDistance(),
                    progression * 100);
            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            FontMetrics fm = g2d.getFontMetrics();
            g2d.drawString(progText, xBarre + largeurBarre / 2 - fm.stringWidth(progText) / 2, yBarre + 17);

            // Légende des zones de dommage
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString("🔴 Zones colorées = dommages qui réduisent la vitesse", xBarre, yBarre + 40);
        }

        private void dessinerLegende(Graphics2D g2d) {
            int x = getWidth() - 180;
            int y = 20;

            g2d.setColor(new Color(255, 255, 255, 230));
            g2d.fillRoundRect(x, y, 160, 110, 10, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawRoundRect(x, y, 160, 110, 10, 10);

            g2d.setFont(new Font("Arial", Font.BOLD, 11));
            g2d.drawString("Légende", x + 50, y + 18);

            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            // Parcouru
            g2d.setColor(new Color(50, 200, 50));
            g2d.fillRect(x + 10, y + 28, 20, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Parcouru", x + 35, y + 37);

            // En cours
            g2d.setColor(new Color(255, 165, 0));
            g2d.fillRect(x + 10, y + 45, 20, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("En cours", x + 35, y + 54);

            // À venir
            g2d.setColor(new Color(180, 180, 180));
            g2d.fillRect(x + 10, y + 62, 20, 10);
            g2d.setColor(Color.BLACK);
            g2d.drawString("À venir", x + 35, y + 71);

            // Dommage
            g2d.setColor(new Color(200, 0, 0));
            g2d.fillOval(x + 10, y + 78, 12, 12);
            g2d.setColor(Color.BLACK);
            g2d.drawString("Arrivée", x + 35, y + 88);
        }
    }
}