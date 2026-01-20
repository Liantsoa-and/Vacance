package model;

public class Degat {
    private Integer id;
    private Integer cheminId;
    private double pointKm;
    private double surfaceM2;
    private double profondeurM;
    private Integer materiauId;

    public Degat() {
    }

    public Degat(Integer id, Integer cheminId, double pointKm, double surfaceM2,
            double profondeurM, Integer materiauId) {
        this.id = id;
        this.cheminId = cheminId;
        this.pointKm = pointKm;
        this.surfaceM2 = surfaceM2;
        this.profondeurM = profondeurM;
        this.materiauId = materiauId;
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

    public Integer getMateriauId() {
        return materiauId;
    }

    public void setMateriauId(Integer materiauId) {
        this.materiauId = materiauId;
    }
}
