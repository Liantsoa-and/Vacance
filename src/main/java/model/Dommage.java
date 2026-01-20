package model;

public class Dommage {
    int id;
    int cheminId;
    double debutKm;
    double finKm;
    double taux;

    public Dommage(int id, int cheminId, double debutKm, double finKm, double taux) {
        this.id = id;
        this.cheminId = cheminId;
        this.debutKm = debutKm;
        this.finKm = finKm;
        this.taux = taux;
    }

    public Dommage() {
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
    public double getTaux() {
        return taux;
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
    public void setTaux(double taux) {
        this.taux = taux;
    }
}
