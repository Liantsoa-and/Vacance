package model;

public class ReparationDegat {
    private Integer id;
    private Integer degatId;
    private Integer materiauId;
    private boolean validee;
    private double coutReparation;

    // Informations pour affichage
    private String degatInfo; // ex: "Chemin A - Point 5.2km"
    private String materiauNom;
    private double surfaceM2;
    private double profondeurM;

    public ReparationDegat() {
    }

    public ReparationDegat(Integer id, Integer degatId, Integer materiauId, boolean validee) {
        this.id = id;
        this.degatId = degatId;
        this.materiauId = materiauId;
        this.validee = validee;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDegatId() {
        return degatId;
    }

    public void setDegatId(Integer degatId) {
        this.degatId = degatId;
    }

    public Integer getMateriauId() {
        return materiauId;
    }

    public void setMateriauId(Integer materiauId) {
        this.materiauId = materiauId;
    }

    public boolean isValidee() {
        return validee;
    }

    public void setValidee(boolean validee) {
        this.validee = validee;
    }

    public double getCoutReparation() {
        return coutReparation;
    }

    public void setCoutReparation(double coutReparation) {
        this.coutReparation = coutReparation;
    }

    public String getDegatInfo() {
        return degatInfo;
    }

    public void setDegatInfo(String degatInfo) {
        this.degatInfo = degatInfo;
    }

    public String getMateriauNom() {
        return materiauNom;
    }

    public void setMateriauNom(String materiauNom) {
        this.materiauNom = materiauNom;
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
        return degatInfo != null ? degatInfo : "ReparationDegat #" + id;
    }
}