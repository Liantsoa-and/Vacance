package model;

public class Degat {
    private Integer id;
    private Integer cheminId;
    private double pointKm;
    private double surfaceM2;
    private double profondeurM;

    public Degat() {
    }

    public Degat(Integer id, Integer cheminId, double pointKm, double surfaceM2,
            double profondeurM) {
        this.id = id;
        this.cheminId = cheminId;
        this.pointKm = pointKm;
        this.surfaceM2 = surfaceM2;
        this.profondeurM = profondeurM;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getCheminId() {
        return cheminId;
    }

    public void setCheminId(Integer cheminId) {
        this.cheminId = cheminId;
    }

    public double getPointKm() {
        return pointKm;
    }

    public void setPointKm(double pointKm) {
        this.pointKm = pointKm;
    }

    public double getSurfaceM2() {
        return surfaceM2;
    }

    public void setSurfaceM2(double surfaceM2) {
        this.surfaceM2 = surfaceM2;
    }

    public double getProfondeurM() {
        return profondeurM;
    }

    public void setProfondeurM(double profondeurM) {
        this.profondeurM = profondeurM;
    }

    @Override
    public String toString() {
        return String.format("Point %.2f km (Surf: %.2f m², Prof: %.2f m)",
                pointKm, surfaceM2, profondeurM);
    }
}
