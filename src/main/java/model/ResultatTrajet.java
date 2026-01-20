package model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;

public class ResultatTrajet {

    private Vehicule vehicule;
    private List<Chemin> itineraire;
    private double tempsTotalHeures;
    private double distanceTotaleKm;
    private boolean conforme;

    // Nouveaux attributs pour la gestion des horaires
    private Date heureDepart;
    private Date heureArrivee;
    private double tempsAttenteTotalHeures; // Temps d'attente aux pauses
    private double vitesseMoyenneReelle; // Vitesse moyenne réelle avec dommages
    private double coutReparationAr; // Coût total de réparation en Ariary

    public ResultatTrajet() {
        this.itineraire = new ArrayList<>();
        this.tempsTotalHeures = 0.0;
        this.distanceTotaleKm = 0.0;
        this.conforme = true;
        this.tempsAttenteTotalHeures = 0.0;
        this.coutReparationAr = 0.0;
    }

    public ResultatTrajet(Vehicule vehicule, List<Chemin> itineraire, double tempsTotalHeures,
            double distanceTotaleKm, boolean conforme) {
        this.vehicule = vehicule;
        this.itineraire = itineraire;
        this.tempsTotalHeures = tempsTotalHeures;
        this.distanceTotaleKm = distanceTotaleKm;
        this.conforme = conforme;
        this.tempsAttenteTotalHeures = 0.0;
        this.vitesseMoyenneReelle = 0.0;
        this.coutReparationAr = 0.0;
    }

    // ================== GETTERS & SETTERS ==================

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }

    public List<Chemin> getItineraire() {
        return itineraire;
    }

    public void setItineraire(List<Chemin> itineraire) {
        this.itineraire = itineraire;
    }

    public double getTempsTotalHeures() {
        return tempsTotalHeures;
    }

    public void setTempsTotalHeures(double tempsTotalHeures) {
        this.tempsTotalHeures = tempsTotalHeures;
    }

    public double getDistanceTotaleKm() {
        return distanceTotaleKm;
    }

    public void setDistanceTotaleKm(double distanceTotaleKm) {
        this.distanceTotaleKm = distanceTotaleKm;
    }

    public boolean isConforme() {
        return conforme;
    }

    public void setConforme(boolean conforme) {
        this.conforme = conforme;
    }

    public Date getHeureDepart() {
        return heureDepart;
    }

    public void setHeureDepart(Date heureDepart) {
        this.heureDepart = heureDepart;
    }

    public Date getHeureArrivee() {
        return heureArrivee;
    }

    public void setHeureArrivee(Date heureArrivee) {
        this.heureArrivee = heureArrivee;
    }

    public double getTempsAttenteTotalHeures() {
        return tempsAttenteTotalHeures;
    }

    public void setTempsAttenteTotalHeures(double tempsAttenteTotalHeures) {
        this.tempsAttenteTotalHeures = tempsAttenteTotalHeures;
    }

    public double getVitesseMoyenneReelle() {
        return vitesseMoyenneReelle;
    }

    public void setVitesseMoyenneReelle(double vitesseMoyenneReelle) {
        this.vitesseMoyenneReelle = vitesseMoyenneReelle;
    }

    public double getCoutReparationAr() {
        return coutReparationAr;
    }

    public void setCoutReparationAr(double coutReparationAr) {
        this.coutReparationAr = coutReparationAr;
    }

    /**
     * Retourne le coût de réparation formaté
     */
    public String getCoutReparationFormate() {
        return String.format("%.2f Ar", coutReparationAr);
    }

    /**
     * Retourne le temps total réel incluant les attentes aux pauses
     */
    public double getTempsTotalAvecPausesHeures() {
        return tempsTotalHeures + tempsAttenteTotalHeures;
    }

    public double calculerConsommationCarburant() {
        if (vehicule == null) {
            return 0.0;
        }
        return vehicule.calculerConsommation(distanceTotaleKm);
    }

    public boolean peutFaireTrajetAvecPleinReservoir() {
        if (vehicule == null) {
            return false;
        }
        double consommation = calculerConsommationCarburant();
        return consommation <= vehicule.getReservoirL();
    }

    public String getTempsFormate() {
        return formatDuree(tempsTotalHeures);
    }

    public String getTempsTotalAvecPausesFormate() {
        return formatDuree(getTempsTotalAvecPausesHeures());
    }

    public String getTempsAttenteFormate() {
        return formatDuree(tempsAttenteTotalHeures);
    }

    private String formatDuree(double heures) {
        if (heures < 0) {
            return "0j 0h 00m";
        }

        long totalSecondes = (long) (heures * 3600.0);
        long jours = totalSecondes / (24 * 3600);
        long reste = totalSecondes % (24 * 3600);
        long h = reste / 3600;
        reste = reste % 3600;
        long minutes = reste / 60;

        if (jours > 0) {
            return String.format("%dj %dh %02dm", jours, h, minutes);
        } else {
            return String.format("%dh %02dm", h, minutes);
        }
    }

    public String getHeureFormatee(Date date) {
        if (date == null) {
            return "-";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        return sdf.toString();
    }

    public String getDescriptionItineraire() {
        if (itineraire == null || itineraire.isEmpty()) {
            return "Aucun chemin trouvé.";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < itineraire.size(); i++) {
            Chemin c = itineraire.get(i);
            sb.append(c.getNom());

            if (i < itineraire.size() - 1) {
                sb.append(" → ");
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        StringBuilder sb = new StringBuilder();

        sb.append("Voyage avec ").append(vehicule != null ? vehicule.getNom() : "?");
        sb.append("\nItinéraire: ").append(getDescriptionItineraire());
        sb.append("\nDistance: ").append(distanceTotaleKm).append(" km");
        sb.append("\nTemps de route: ").append(getTempsFormate());

        if (tempsAttenteTotalHeures > 0) {
            sb.append("\nTemps d'attente aux pauses: ").append(getTempsAttenteFormate());
            sb.append("\nTemps total: ").append(getTempsTotalAvecPausesFormate());
        }

        if (heureDepart != null) {
            sb.append("\nDépart: ").append(sdf.format(heureDepart));
        }
        if (heureArrivee != null) {
            sb.append("\nArrivée: ").append(sdf.format(heureArrivee));
        }

        sb.append("\nConforme: ").append(conforme ? "Oui" : "Non");

        return sb.toString();
    }
}