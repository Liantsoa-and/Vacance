package model;

public class Reparation {
    private Integer id;
    private Integer materiauId;
    private String materiauNom; // Pour affichage
    private double profondeurMin;
    private double profondeurMax;
    private double prixParM2;
    private String description;

    public Reparation() {
    }

    public Reparation(Integer id, Integer materiauId, double profondeurMin, double profondeurMax,
            double prixParM2, String description) {
        this.id = id;
        this.materiauId = materiauId;
        this.profondeurMin = profondeurMin;
        this.profondeurMax = profondeurMax;
        this.prixParM2 = prixParM2;
        this.description = description;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMateriauId() {
        return materiauId;
    }

    public void setMateriauId(Integer materiauId) {
        this.materiauId = materiauId;
    }

    public String getMateriauNom() {
        return materiauNom;
    }

    public void setMateriauNom(String materiauNom) {
        this.materiauNom = materiauNom;
    }

    public double getProfondeurMin() {
        return profondeurMin;
    }

    public void setProfondeurMin(double profondeurMin) {
        this.profondeurMin = profondeurMin;
    }

    public double getProfondeurMax() {
        return profondeurMax;
    }

    public void setProfondeurMax(double profondeurMax) {
        this.profondeurMax = profondeurMax;
    }

    public double getPrixParM2() {
        return prixParM2;
    }

    public void setPrixParM2(double prixParM2) {
        this.prixParM2 = prixParM2;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIntervalleFormate() {
        return String.format("[%.2f - %.2f m]", profondeurMin, profondeurMax);
    }
}
