package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import model.Dommage;
import inc.OracleConnection;

public class DommageDAO {
    public List<Dommage> getByCheminId(int cheminId) throws SQLException {
        List<Dommage> dommages = new ArrayList<>();

        String sql = "SELECT * FROM DOMMAGE WHERE chemin_id = ? ORDER BY id";

        try (Connection conn = OracleConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cheminId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("id");
                    double debutKm = rs.getDouble("debut_km");
                    double finKm = rs.getDouble("fin_km");
                    double reductionTaux = rs.getDouble("reduction_taux");

                    Dommage dommage = new Dommage(id, cheminId, debutKm, finKm, reductionTaux);
                    dommages.add(dommage);
                }
            }
        }
        return dommages;
    }
}
