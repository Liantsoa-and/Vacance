package model;

public class Vehicule {
    int id;
    String nom;
    String type;
    double vitesseMax;
    double largeur;
    double longueur;
    double reservoirL; 
    double consommationL100km; 

    public Vehicule(int id, String nom, String type, double vitesseMax, double largeur, double longueur,
            double reservoirL, double consommationL100km) {
        this.id = id;
        this.nom = nom;
        this.type = type;
        this.vitesseMax = vitesseMax;
        this.largeur = largeur;
        this.longueur = longueur;
        this.reservoirL = reservoirL;
        this.consommationL100km = consommationL100km;
    }

    public Vehicule() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    public void setVitesseMax(double vitesseMax) {
        if (vitesseMax < 0) {
            throw new IllegalArgumentException("La vitesse maximale ne peut pas être négative");
        }
        this.vitesseMax = vitesseMax;
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

    public double getLongueur() {
        return longueur;
    }

    public void setLongueur(double longueur) {
        if (longueur < 0) {
            throw new IllegalArgumentException("La longueur ne peut pas être négative");
        }
        this.longueur = longueur;
    }

    public double getReservoirL() {
        return reservoirL;
    }

    public void setReservoirL(double reservoirL) {
        if (reservoirL < 0) {
            throw new IllegalArgumentException("La capacité du réservoir ne peut pas être négative");
        }
        this.reservoirL = reservoirL;
    }

    public double getConsommationL100km() {
        return consommationL100km;
    }

    public void setConsommationL100km(double consommationL100km) {
        if (consommationL100km < 0) {
            throw new IllegalArgumentException("La consommation ne peut pas être négative");
        }
        this.consommationL100km = consommationL100km;
    }

    public double calculerAutonomie() {
        if (consommationL100km <= 0) {
            return 0;
        }
        return (reservoirL / consommationL100km) * 100;
    }

    public double calculerConsommation(double distanceKm) {
        return (distanceKm / 100.0) * consommationL100km;
    }

}
