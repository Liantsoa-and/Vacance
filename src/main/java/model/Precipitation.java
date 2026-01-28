package model;

public class Precipitation {
int id;
    int cheminId;
    double debutKm;
    double finKm;
    double niveauMm;

    public Precipitation(int id, int cheminId, double debutKm, double finKm, double niveauMm) {
        this.id = id;
        this.cheminId = cheminId;
        this.debutKm = debutKm;
        this.finKm = finKm;
        this.niveauMm = niveauMm;
    }

    public Precipitation() {
    }

    //Getter
    public int getId() {
        return id;
    }
    public int getCheminId() {
        return cheminId;
    }
    public double getDebutKm() {
        return debutKm;
    }
    public double getFinKm() {
        return finKm;
    }
    public double getNiveauMm() {
        return niveauMm;
    }

    //Setter
    public void setId(int id) {
        this.id = id;
    }
    public void setCheminId(int cheminId) {
        this.cheminId = cheminId;
    }
    public void setDebutKm(double debutKm) {
        this.debutKm = debutKm;
    }
    public void setFinKm(double finKm) {
        this.finKm = finKm;
    }
    public void setNiveauMm(double niveauMm) {
        this.niveauMm = niveauMm;
    }

    //utilitaire
    @Override
    public String toString() {
        return "Precipitation{" +
                "id=" + id +
                ", cheminId=" + cheminId +
                ", debutKm=" + debutKm +
                ", finKm=" + finKm +
                ", niveauMm=" + niveauMm +
                '}';
    }

    public boolean containsKm(double km) {
        return km >= debutKm && km <= finKm;
    }
}