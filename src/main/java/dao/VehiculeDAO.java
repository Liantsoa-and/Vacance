package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import inc.PostgresConnection;
import model.Vehicule;

public class VehiculeDAO {
    public List<Vehicule> getAll() {
        List<Vehicule> listeVehicules = new ArrayList<>();

        String sql = "SELECT * FROM VEHICULE ORDER BY nom";

        try (Connection conn = PostgresConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String nom = rs.getString("nom");
                String type = rs.getString("type");
                double vitesseMax = rs.getDouble("vitesse_max");
                double largeur_m = rs.getDouble("largeur_m");
                double longueur_m = rs.getDouble("longueur_m");
                double reservoirL = rs.getDouble("reservoir_l");
                double consommationL100km = rs.getDouble("consommation_l100km");

                Vehicule Vehicule = new Vehicule(id, nom, type, vitesseMax, largeur_m, longueur_m, reservoirL,
                        consommationL100km);

                listeVehicules.add(Vehicule);
            }

        } catch (SQLException e) {
            System.err.println("Erreur SQL dans VehiculeDAO.getAll() : " + e.getMessage());
            e.printStackTrace();
        }

        return listeVehicules;
    }

    public Vehicule getById(int idCherche) {
        Vehicule Vehicule = null;
        String sql = "SELECT * FROM VEHICULE WHERE id = ?";

        try (Connection conn = PostgresConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, idCherche);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Vehicule = new Vehicule(
                            rs.getInt("id"),
                            rs.getString("nom"),
                            rs.getString("type"),
                            rs.getDouble("vitesse_max"),
                            rs.getDouble("largeur_m"),
                            rs.getDouble("longueur_m"),
                            rs.getDouble("reservoir_l"),
                            rs.getDouble("consommation_l100km"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return Vehicule;
    }
}
