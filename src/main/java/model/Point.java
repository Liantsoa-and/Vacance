package model;

public class Point {
    int id;
    String nom;

    public Point(int id, String nom) {
        this.id = id;
        this.nom = nom;
    }

    public Point() {
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
}
