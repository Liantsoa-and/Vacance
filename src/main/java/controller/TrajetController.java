package controller;

import java.util.*;
import java.sql.SQLException;
import dao.*;
import model.*;

public class TrajetController {

    /**
     * Classe interne pour représenter un passage par une pause
     */
    private static class PassagePause {
        Pause pause;
        Date heurePassage;
        double tempsAttente;

        public PassagePause(Pause pause, Date heurePassage, double tempsAttente) {
            this.pause = pause;
            this.heurePassage = heurePassage;
            this.tempsAttente = tempsAttente;
        }
    }

    /**
     * Classe interne pour représenter un segment de route avec sa vitesse
     */
    private static class SegmentRoute {
        double debutKm;
        double finKm;
        double vitesse;

        public SegmentRoute(double debutKm, double finKm, double vitesse) {
            this.debutKm = debutKm;
            this.finKm = finKm;
            this.vitesse = vitesse;
        }

        public double getDistance() {
            return finKm - debutKm;
        }

        public double getTemps() {
            return getDistance() / vitesse;
        }
    }

    /**
     * Extrait les composants de temps d'une date (heures, minutes, secondes)
     */
    private static int[] extraireComposantsTemps(Date date) {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        return new int[] {
                cal.get(java.util.Calendar.HOUR_OF_DAY),
                cal.get(java.util.Calendar.MINUTE),
                cal.get(java.util.Calendar.SECOND)
        };
    }

    /**
     * Crée une liste de segments d'un chemin avec les vitesses appropriées selon
     * les dommages
     */
    private static List<SegmentRoute> creerSegmentsAvecDommages(Chemin chemin, List<Dommage> dommages,
            double vitesseMoyenne, double debutKm, double finKm) {
        List<SegmentRoute> segments = new ArrayList<>();

        if (dommages == null || dommages.isEmpty()) {
            segments.add(new SegmentRoute(debutKm, finKm, vitesseMoyenne));
            return segments;
        }

        Collections.sort(dommages, Comparator.comparingDouble(Dommage::getDebutKm));
        double positionActuelle = debutKm;

        for (Dommage d : dommages) {
            if (d.getFinKm() <= debutKm || d.getDebutKm() >= finKm) {
                continue;
            }

            // Segment avant le dommage
            if (d.getDebutKm() > positionActuelle) {
                double fin = Math.min(d.getDebutKm(), finKm);
                segments.add(new SegmentRoute(positionActuelle, fin, vitesseMoyenne));
                positionActuelle = fin;
            }

            // Segment dans le dommage
            double debutDommage = Math.max(d.getDebutKm(), positionActuelle);
            double finDommage = Math.min(d.getFinKm(), finKm);

            if (finDommage > debutDommage) {
                double vitesseReduite = vitesseMoyenne * (1.0 - d.getTaux());
                segments.add(new SegmentRoute(debutDommage, finDommage, vitesseReduite));
                positionActuelle = finDommage;
            }

            if (positionActuelle >= finKm) {
                break;
            }
        }

        // Segment final après le dernier dommage
        if (positionActuelle < finKm) {
            segments.add(new SegmentRoute(positionActuelle, finKm, vitesseMoyenne));
        }

        return segments;
    }

    /**
     * Calcule la vitesse moyenne réelle pour un seul chemin en tenant compte des
     * dommages
     * Utilise la formule de moyenne pondérée par la distance:
     * vitesse_moyenne = somme(vitesse_section * distance_section) / distance_totale
     */
    public static double calculerVitesseMoyenneReelleChemin(Chemin chemin, List<Dommage> dommages,
            double vitesseMoyenne) {
        if (chemin == null) {
            return 0.0;
        }

        double distanceChemin = chemin.getDistance();
        List<SegmentRoute> segments = creerSegmentsAvecDommages(chemin, dommages, vitesseMoyenne, 0.0, distanceChemin);

        double sommePonderee = 0.0;
        for (SegmentRoute segment : segments) {
            sommePonderee += segment.vitesse * segment.getDistance();
        }

        return sommePonderee / distanceChemin;
    }

    /**
     * Calcule la vitesse moyenne réelle pour tout un itinéraire (plusieurs chemins)
     * Formule: vitesse_moyenne_réelle_itinéraire =
     * somme(vitesse_section * distance_section) / distance_totale
     */
    public static double calculerVitesseMoyenneReelleItineraire(List<Chemin> itineraire, double vitesseMoyenne)
            throws SQLException {

        if (itineraire == null || itineraire.isEmpty()) {
            return 0.0;
        }

        double distanceTotale = 0.0;
        double sommePonderee = 0.0;

        DommageDAO daoDommage = new DommageDAO();

        for (Chemin chemin : itineraire) {
            double distanceChemin = chemin.getDistance();
            distanceTotale += distanceChemin;

            List<Dommage> dommages = daoDommage.getByCheminId(chemin.getId());

            double vitesseCheminPonderee = calculerVitesseMoyenneReelleChemin(chemin, dommages, vitesseMoyenne);

            sommePonderee += (vitesseCheminPonderee * distanceChemin);
        }

        if (distanceTotale > 0) {
            return sommePonderee / distanceTotale;
        }
        return 0.0;
    }

    public static double calculerTemps(Chemin c, Vehicule v, List<Dommage> dommages, double vitesseMoyenne) {
        if (vitesseMoyenne <= 0) {
            throw new IllegalArgumentException("La vitesse moyenne doit être positive");
        }

        return calculerTempsPartiel(c, 0.0, c.getDistance(), dommages, vitesseMoyenne);
    }

    private static String heureCouranteString(Date date) {
        int[] temps = extraireComposantsTemps(date);
        return String.format("%02d:%02d:%02d", temps[0], temps[1], temps[2]);
    }

    public static ResultatTrajet calculerHoraires(List<Chemin> itineraire, Date heureDepart,
            Vehicule vehicule, double vitesseMoyenne)
            throws SQLException {

        System.out.println("\n=== CALCUL DES HORAIRES ===");
        System.out.println("Heure de départ: " + heureCouranteString(heureDepart));
        System.out.println("Vitesse moyenne: " + vitesseMoyenne + " km/h\n");

        DommageDAO daoDommage = new DommageDAO();
        PauseDAO daoPause = new PauseDAO();

        Date heureCourante = new Date(heureDepart.getTime());
        double tempsRouteTotal = 0.0;
        double tempsAttenteTotal = 0.0;
        double distanceTotale = 0.0;
        boolean conforme = true;

        List<PassagePause> pausesRencontrees = new ArrayList<>();

        for (int i = 0; i < itineraire.size(); i++) {
            Chemin chemin = itineraire.get(i);
            distanceTotale += chemin.getDistance();

            if (!RouteController.peutPasser(vehicule, chemin)) {
                conforme = false;
            }

            List<Dommage> dommages = daoDommage.getByCheminId(chemin.getId());
            double tempsCheminHeures = calculerTemps(chemin, vehicule, dommages, vitesseMoyenne);

            List<Pause> pauses = daoPause.getByCheminId(chemin.getId());

            int pointArriveePrec = (i > 0) ? itineraire.get(i - 1).getPointFin() : chemin.getPointDebut();
            boolean estInverse = (pointArriveePrec == chemin.getPointFin());

            if (estInverse && !pauses.isEmpty()) {
                for (Pause p : pauses) {
                    p.setPointKm(chemin.getDistance() - p.getPointKm());
                }
            }

            if (pauses.isEmpty()) {
                tempsRouteTotal += tempsCheminHeures;
                heureCourante = ajouterHeures(heureCourante, tempsCheminHeures);
            } else {
                Collections.sort(pauses, Comparator.comparingDouble(Pause::getPointKm));

                double positionKm = 0.0;

                for (Pause pause : pauses) {
                    double distanceJusquaPause = pause.getPointKm() - positionKm;

                    if (distanceJusquaPause > 0) {
                        // Calculer le temps pour cette portion en tenant compte des dommages
                        double tempsJusquaPause = calculerTempsPartiel(chemin, positionKm,
                                pause.getPointKm(), dommages, vitesseMoyenne);

                        tempsRouteTotal += tempsJusquaPause;
                        heureCourante = ajouterHeures(heureCourante, tempsJusquaPause);
                    }

                    // Vérifier si on arrive pendant la pause
                    double tempsAttente = calculerTempsAttente(heureCourante, pause);

                    if (tempsAttente > 0) {
                        System.out.println(
                                "⏸️  Pause rencontrée: Heure d'arrivée = " + heureCouranteString(heureCourante) +
                                        " | Pause: " + pause.getHeureDebut() + " à " + pause.getHeureFin() +
                                        " | Temps d'attente: " + formatDuree(tempsAttente));
                        pausesRencontrees.add(new PassagePause(pause,
                                new Date(heureCourante.getTime()), tempsAttente));
                        tempsAttenteTotal += tempsAttente;
                        heureCourante = ajouterHeures(heureCourante, tempsAttente);
                    } else {
                        System.out.println("✓ Pause dépassée: Heure d'arrivée = " + heureCouranteString(heureCourante) +
                                " | Pause: " + pause.getHeureDebut() + " à " + pause.getHeureFin() +
                                " | Aucune attente");
                    }

                    positionKm = pause.getPointKm();
                }

                double distanceRestante = chemin.getDistance() - positionKm;
                if (distanceRestante > 0) {
                    double tempsRestant = calculerTempsPartiel(chemin, positionKm,
                            chemin.getDistance(), dommages, vitesseMoyenne);
                    tempsRouteTotal += tempsRestant;
                    heureCourante = ajouterHeures(heureCourante, tempsRestant);
                }
            }
        }

        double vitesseMoyenneReelle = calculerVitesseMoyenneReelleItineraire(itineraire, vitesseMoyenne);
        System.out.println(
                "Vitesse moyenne réelle (avec dommages): " + String.format("%.2f", vitesseMoyenneReelle) + " km/h\n");

        // Calcul du coût de réparation pour cet itinéraire
        double coutReparation = 0.0;
        try {
            List<Integer> cheminIds = new ArrayList<>();
            for (Chemin c : itineraire) {
                cheminIds.add(c.getId());
            }
            ReparationDAO reparationDAO = new ReparationDAO();
            coutReparation = reparationDAO.calculerCoutPourChemins(cheminIds);
        } catch (SQLException e) {
            System.err.println("Erreur lors du calcul du coût de réparation: " + e.getMessage());
        }

        ResultatTrajet resultat = new ResultatTrajet(vehicule, itineraire, tempsRouteTotal,
                distanceTotale, conforme);
        resultat.setHeureDepart(heureDepart);
        resultat.setHeureArrivee(heureCourante);
        resultat.setTempsAttenteTotalHeures(tempsAttenteTotal);
        resultat.setVitesseMoyenneReelle(vitesseMoyenneReelle);
        resultat.setCoutReparationAr(coutReparation);

        if (!resultat.peutFaireTrajetAvecPleinReservoir()) {
            resultat.setConforme(false);
        }

        return resultat;
    }

    /**
     * Calcule le temps d'attente à une pause
     * Une pause est un créneau obligatoire : si on arrive PENDANT, on attend
     * jusqu'à la fin
     * Si on arrive avant ou après, on peut continuer sans attendre
     * Compare juste les heures du jour, pas les dates complètes
     */
    private static double calculerTempsAttente(Date heureArrivee, Pause pause) {
        // Extraire juste les heures du jour (ignorer la date)
        int[] tempsArrivee = extraireComposantsTemps(heureArrivee);
        int[] tempsDebut = extraireComposantsTemps(pause.getHeureDebut());
        int[] tempsFin = extraireComposantsTemps(pause.getHeureFin());

        // Convertir en secondes pour la comparaison
        long secsArrivee = tempsArrivee[0] * 3600 + tempsArrivee[1] * 60 + tempsArrivee[2];
        long secsDebut = tempsDebut[0] * 3600 + tempsDebut[1] * 60 + tempsDebut[2];
        long secsFin = tempsFin[0] * 3600 + tempsFin[1] * 60 + tempsFin[2];

        System.out.println("  📊 Calcul attente pause:");
        System.out.println("    - Heure arrivée: "
                + String.format("%02d:%02d:%02d", tempsArrivee[0], tempsArrivee[1], tempsArrivee[2]));
        System.out.println(
                "    - Début pause:   " + String.format("%02d:%02d:%02d", tempsDebut[0], tempsDebut[1], tempsDebut[2]));
        System.out.println(
                "    - Fin pause:     " + String.format("%02d:%02d:%02d", tempsFin[0], tempsFin[1], tempsFin[2]));

        if (secsArrivee < secsDebut) {
            System.out.println("    → Arrivée AVANT la pause → Pas d'attente");
            return 0.0;
        }

        if (secsArrivee >= secsDebut && secsArrivee < secsFin) {
            long secsAttente = secsFin - secsArrivee;
            double heuresAttente = secsAttente / (60.0 * 60.0);
            System.out.println("    → Arrivée PENDANT la pause → Attente de " + formatDuree(heuresAttente));
            return heuresAttente;
        }

        System.out.println("    → Arrivée APRÈS la pause → Pas d'attente");
        return 0.0;
    }

    /**
     * Calcule le temps pour parcourir une portion d'un chemin
     */
    private static double calculerTempsPartiel(Chemin chemin, double kmDebut, double kmFin,
            List<Dommage> dommages, double vitesseMoyenne) {
        if (kmFin <= kmDebut) {
            return 0.0;
        }

        List<SegmentRoute> segments = creerSegmentsAvecDommages(chemin, dommages, vitesseMoyenne, kmDebut, kmFin);

        double tempsTotal = 0.0;
        for (SegmentRoute segment : segments) {
            if (segment.vitesse > 0) {
                tempsTotal += segment.getTemps();
            } else {
                throw new IllegalArgumentException("La vitesse réduite est nulle ou négative");
            }
        }

        return tempsTotal;
    }

    /**
     * Ajoute des heures à une date
     */
    private static Date ajouterHeures(Date date, double heures) {
        long ms = (long) (heures * 3600 * 1000);
        return new Date(date.getTime() + ms);
    }

    public static String formatDuree(double tempsEnHeures) {
        if (tempsEnHeures < 0) {
            return "0j 0h 00m 00s";
        }

        long totalSecondes = (long) (tempsEnHeures * 3600.0);
        long jours = totalSecondes / (24 * 3600);
        long reste = totalSecondes % (24 * 3600);
        long heures = reste / 3600;
        reste = reste % 3600;
        long minutes = reste / 60;
        long secondes = reste % 60;

        return String.format("%dj %dh %02dm %02ds", jours, heures, minutes, secondes);
    }

    public static List<ResultatTrajet> trouverCheminsLesPlusCourts(int idPointDepart, int idPointArrivee,
            Vehicule vehicule, double vitesseMoyenne, Date heureDepart) throws SQLException {

        PointDAO daoPoint = new PointDAO();
        List<Point> points = daoPoint.getAll();
        CheminDAO daoChemin = new CheminDAO();
        List<Chemin> chemins = daoChemin.getAll();
        DommageDAO daoDommage = new DommageDAO();

        Map<Integer, List<Integer>> graphe = new HashMap<>();
        Map<String, Chemin> mapChemins = new HashMap<>();

        for (Point p : points) {
            graphe.put(p.getId(), new ArrayList<>());
        }

        for (Chemin c : chemins) {
            graphe.get(c.getPointDebut()).add(c.getPointFin());
            graphe.get(c.getPointFin()).add(c.getPointDebut());
            mapChemins.put(c.getPointDebut() + "-" + c.getPointFin(), c);
            mapChemins.put(c.getPointFin() + "-" + c.getPointDebut(), c);
        }

        List<List<Integer>> tousLesChemins = new ArrayList<>();
        Set<Integer> visites = new HashSet<>();
        List<Integer> cheminCourant = new ArrayList<>();
        dfs(idPointDepart, idPointArrivee, graphe, visites, cheminCourant, tousLesChemins);

        List<ResultatTrajet> resultats = new ArrayList<>();

        for (List<Integer> p : tousLesChemins) {
            List<Chemin> itineraire = new ArrayList<>();

            for (int i = 0; i < p.size() - 1; i++) {
                int p1 = p.get(i);
                int p2 = p.get(i + 1);
                Chemin c = mapChemins.get(p1 + "-" + p2);
                if (c != null) {
                    itineraire.add(c);
                }
            }

            ResultatTrajet rt = calculerHoraires(itineraire, heureDepart, vehicule, vitesseMoyenne);
            resultats.add(rt);
        }

        resultats.sort(Comparator.comparingDouble(ResultatTrajet::getTempsTotalAvecPausesHeures));

        return resultats;
    }

    private static void dfs(int courant, int destination, Map<Integer, List<Integer>> graphe,
            Set<Integer> visites, List<Integer> cheminCourant,
            List<List<Integer>> tousLesChemins) {
        visites.add(courant);
        cheminCourant.add(courant);

        if (courant == destination) {
            tousLesChemins.add(new ArrayList<>(cheminCourant));
        } else {
            for (int voisin : graphe.get(courant)) {
                if (!visites.contains(voisin)) {
                    dfs(voisin, destination, graphe, visites, cheminCourant, tousLesChemins);
                }
            }
        }

        cheminCourant.remove(cheminCourant.size() - 1);
        visites.remove(courant);
    }
}