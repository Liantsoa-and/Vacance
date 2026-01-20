-- ==========================================
-- NETTOYAGE
-- ==========================================
DROP TABLE IF EXISTS vehicule CASCADE;

-- ==========================================
-- CREATION DE LA TABLE
-- ==========================================
CREATE TABLE vehicule (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    vitesse_max INT NOT NULL,
    largeur_m DECIMAL(5, 2) NOT NULL,
    longueur_m DECIMAL(5, 2) NOT NULL,
    reservoir_l DECIMAL(5, 2) NOT NULL,
    consommation_l100km DECIMAL(5, 2) NOT NULL
);

-- ==========================================
-- INSERTION DES DONNEES D'EXEMPLE
-- ==========================================

-- Peugeot 308 : Voiture, 200km/h, 1.80m, 4.25m, 60L réservoir, 6.5 l/100km
INSERT INTO vehicule (nom, type, vitesse_max, largeur_m, longueur_m, reservoir_l, consommation_l100km)
VALUES ('Peugeot 308', 'Voiture', 200, 1.80, 4.25, 50, 9);



COMMIT;