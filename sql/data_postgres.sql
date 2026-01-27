-- ==========================================
-- INSERTION DES DONNEES D'EXEMPLE
-- ==========================================

-- Peugeot 308 : Voiture, 200km/h, 1.80m, 4.25m, 60L réservoir, 6.5 l/100km
INSERT INTO vehicule (nom, type, vitesse_max, largeur_m, longueur_m, reservoir_l, consommation_l100km)
VALUES ('Peugeot 308', 'Voiture', 200, 1.80, 4.25, 50, 9);

COMMIT;