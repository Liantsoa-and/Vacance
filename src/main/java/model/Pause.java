package model;

import java.util.Date;

public class Pause {
    int id;
    int cheminId;
    double pointKm;
    Date heureDebut;
    Date heureFin;

    public Pause(int id, int cheminId, double pointKm, Date heureDebut, Date heureFin) {
        this.id = id;
        this.cheminId = cheminId;
        this.pointKm = pointKm;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
    }

    public Pause() {
    }

    // Getters
    public int getId() {
        return id;
    }

    public int getCheminId() {
        return cheminId;
    }

    public double getPointKm() {
        return pointKm;
    }

    public Date getHeureDebut() {
        return heureDebut;
    }

    public Date getHeureFin() {
        return heureFin;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setCheminId(int cheminId) {
        this.cheminId = cheminId;
    }

    public void setPointKm(double pointKm) {
        this.pointKm = pointKm;
    }

    public void setHeureDebut(Date heureDebut) {
        this.heureDebut = heureDebut;
    }

    public void setHeureFin(Date heureFin) {
        this.heureFin = heureFin;
    }

    /**
     * Calcule la durée de la pause en heures
     */
    public double getDureeHeures() {
        if (heureDebut == null || heureFin == null) {
            return 0.0;
        }
        long diffMs = heureFin.getTime() - heureDebut.getTime();
        return diffMs / (1000.0 * 60.0 * 60.0);
    }

    /**
     * Vérifie si un horaire donné tombe pendant la pause
     */
    public boolean estPendantPause(Date horaire) {
        if (horaire == null || heureDebut == null || heureFin == null) {
            return false;
        }
        return !horaire.before(heureDebut) && horaire.before(heureFin);
    }

    @Override
    public String toString() {
        return String.format("Pause à %.1f km de %s à %s (%.1fh)", 
            pointKm, heureDebut, heureFin, getDureeHeures());
    }
}