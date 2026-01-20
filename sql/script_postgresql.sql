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
