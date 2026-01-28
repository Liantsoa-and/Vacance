package controller;

import dao.*;
import model.*;
import java.sql.*;

public class MaterialSelectionService {

    /*
     * Avoir le materiau approprié selon le niveau de précipitation
     */
    public Materiau getMaterialForDommage(int cheminId, double km) throws SQLException {
        PrecipitationDAO precipitationDAO = new PrecipitationDAO();
        MateriauPrecipitationDAO materiauPrecipitationDAO = new MateriauPrecipitationDAO();
        MateriauDAO materiauDAO = new MateriauDAO();

        try {
            // 1. chercher la zone de précipitation
            Precipitation precipitation = precipitationDAO.findByCheminAndKm(cheminId, km);

            // 2. Chercher la zone de précipitation
            double niveauPrecipitation = 0.0;
            if (precipitation != null) {
                niveauPrecipitation = precipitation.getNiveauMm();
            }

            // 3. Chercher le matériau correspondant
            MateriauPrecipitation mp = materiauPrecipitationDAO.findByPrecipitationLevel(niveauPrecipitation);
            if (mp != null) {
                return materiauDAO.getById(mp.getMateriauId());
            }

            // 4. Si aucun matériau trouvé, retourner un materiau par défaut (id=1)
            if (niveauPrecipitation != 0.0) {
                MateriauPrecipitation defaultMp = materiauPrecipitationDAO.findByPrecipitationLevel(0.0);
                if (defaultMp != null) {
                    return materiauDAO.getById(defaultMp.getMateriauId());
                }
            }

            // 5. Si aucun matériau trouvé
            throw new SQLException("Aucun matériau configuré pour le niveau de précipitation " + niveauPrecipitation
                    + "mm sur le chemin " + cheminId + " au km " + km);

        } catch (SQLException e) {
            System.err.println("Erreur lors de la récupération du matériau : " + e.getMessage());
            throw e;
        }

    }
}
