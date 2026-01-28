package model;

public class MateriauPrecipitation {
    int id;
    int materiauId;
    double niveauMin;
    double niveauMax;

    public MateriauPrecipitation(int id, int materiauId, double niveauMin, double niveauMax) {
        this.id = id;
        this.materiauId = materiauId;
        this.niveauMin = niveauMin;
        this.niveauMax = niveauMax;
    }

    public MateriauPrecipitation() {
    }

    // Getter
    public int getId() {
        return id;
    }

    public int getMateriauId() {
        return materiauId;
    }

    public double getNiveauMin() {
        return niveauMin;
    }

    public double getNiveauMax() {
        return niveauMax;
    }

    // Setter
    public void setId(int id) {
        this.id = id;
    }

    public void setMateriauId(int materiauId) {
        this.materiauId = materiauId;
    }

    public void setNiveauMin(double niveauMin) {
        this.niveauMin = niveauMin;
    }

    public void setNiveauMax(double niveauMax) {
        this.niveauMax = niveauMax;
    }

    //utilitaire
    public boolean matches(double niveauPrecipitation) {
        return niveauPrecipitation >= niveauMin && niveauPrecipitation <= niveauMax;
    }
}
