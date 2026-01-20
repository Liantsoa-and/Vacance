package exception;

public class CarburantException extends Exception {
    private double consommationRequise;
    private double capaciteReservoir;
    private double autonomieMax;
    private double distanceTrajet;

    public CarburantException(String message) {
        super(message);
    }

    public CarburantException(String message, double consommationRequise, double capaciteReservoir,
            double autonomieMax, double distanceTrajet) {
        super(message);
        this.consommationRequise = consommationRequise;
        this.capaciteReservoir = capaciteReservoir;
        this.autonomieMax = autonomieMax;
        this.distanceTrajet = distanceTrajet;
    }

    public double getConsommationRequise() {
        return consommationRequise;
    }

    public double getCapaciteReservoir() {
        return capaciteReservoir;
    }

    public double getAutonomieMax() {
        return autonomieMax;
    }

    public double getDistanceTrajet() {
        return distanceTrajet;
    }

    @Override
    public String toString() {
        if (consommationRequise > 0 && capaciteReservoir > 0) {
            return "CarburantException: Le trajet de " + String.format("%.2f", distanceTrajet) + " km nécessite "
                    + String.format("%.2f", consommationRequise) + " L de carburant, "
                    + "mais le réservoir ne peut contenir que " + String.format("%.2f", capaciteReservoir) + " L. "
                    + "Autonomie maximale: " + String.format("%.2f", autonomieMax) + " km";
        }
        return getMessage();
    }
}
