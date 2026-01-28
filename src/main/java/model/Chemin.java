package model;

public class Chemin {
    int id;
    String nom;
    int pointDebut;
    int pointFin;
    double distance;
    double largeur;
    String sens;

    public Chemin(int id, String nom, int pointDebut, int pointFin, double distance, double largeur, String sens) {
        this.id = id;
        this.nom = nom;
        this.pointDebut = pointDebut;
        this.pointFin = pointFin;
        this.distance = distance;
        this.largeur = largeur;
        this.sens = sens;
    }

    public Chemin() {
    }

    // Getter and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getPointDebut() {
        return pointDebut;
    }

    public void setPointDebut(int pointDebut) {
        this.pointDebut = pointDebut;
    }

    public int getPointFin() {
        return pointFin;
    }

    public void setPointFin(int pointFin) {
        this.pointFin = pointFin;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        if (distance < 0) {
            throw new IllegalArgumentException("La distance ne peut pas être négative");
        }
        this.distance = distance;
    }

    public double getLargeur() {
        return largeur;
    }

    public void setLargeur(double largeur) {
        if (largeur < 0) {
            throw new IllegalArgumentException("La largeur ne peut pas être négative");
        }
        this.largeur = largeur;
    }

    public String getSens() {
        return sens;
    }

    public void setSens(String sens) {
        this.sens = sens;
    }

    @Override
    public String toString() {
        return nom;
    }
}
