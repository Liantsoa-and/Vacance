package model;

public class Borne {
    private int id;
    private int cheminId;
    private double km;

    public Borne() {
    }

    public Borne(int id, int cheminId, double km) {
        this.id = id;
        this.cheminId = cheminId;
        this.km = km;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCheminId() {
        return cheminId;
    }

    public void setCheminId(int cheminId) {
        this.cheminId = cheminId;
    }

    public double getKm() {
        return km;
    }

    public void setKm(double km) {
        this.km = km;
    }

    @Override
    public String toString() {
        return "Borne{" +
                "id=" + id +
                ", cheminId=" + cheminId +
                ", km=" + km +
                '}';
    }
}
