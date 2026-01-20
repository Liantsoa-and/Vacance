package controller;

import model.Chemin;
import model.Vehicule;

public class RouteController {

    /*
     * Il faut que je verifie plus tard pour si le chemin est en simple sens ou
     * double sens
     * car si en simple sens alors le seuilAutorise = largeurChemin
     * 
     */
    public static boolean peutPasser(Vehicule v, Chemin c) {
        if (v == null || c == null) {
            return false;
        }

        double largeurVehicule = v.getLargeur();
        double largeurChemin = c.getLargeur();

        // Application de la règle de division par 2
        double seuilAutorise = largeurChemin / 2.0;

        // Affichage des valeurs pour le débogage
        System.out.println("Largeur du véhicule: " + largeurVehicule + " m");
        System.out.println("Largeur du chemin: " + largeurChemin + " m");
        System.out.println("Seuil autorisé (largeur chemin / 2): " + seuilAutorise + " m");

        // On retourne le résultat de la comparaison
        return largeurVehicule < seuilAutorise;
    }
}