package exception;

public class VitesseException extends Exception {
    private double vitesseMoyenne;
    private double vitesseMax;

    public VitesseException(String message) {
        super(message);
    }

    public VitesseException(String message, double vitesseMoyenne, double vitesseMax) {
        super(message);
        this.vitesseMoyenne = vitesseMoyenne;
        this.vitesseMax = vitesseMax;
    }

    public double getVitesseMoyenne() {
        return vitesseMoyenne;
    }

    public double getVitesseMax() {
        return vitesseMax;
    }

    @Override
    public String toString() {
        if (vitesseMoyenne > 0 && vitesseMax > 0) {
            return "VitesseException: La vitesse moyenne (" + String.format("%.2f", vitesseMoyenne)
                    + " km/h) ne peut pas dépasser la vitesse maximale du véhicule ("
                    + String.format("%.2f", vitesseMax) + " km/h)";
        }
        return getMessage();
    }
}
