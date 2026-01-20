-- ==========================================
-- NETTOYAGE DES DONNEES EXISTANTES
-- ==========================================
DELETE FROM REPARATION;
DELETE FROM INTERVALLE_PROFONDEUR;
DELETE FROM DEGAT;

-- ==========================================
-- INSERTION DES INTERVALLES DE PROFONDEUR
-- ==========================================
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(1, 0.00, 0.02, 'Fissures superficielles (0-2 cm)');
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(2, 0.02, 0.05, 'Ornières légères (2-5 cm)');
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(3, 0.05, 0.10, 'Déformations moyennes (5-10 cm)');
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(4, 0.10, 0.20, 'Nids-de-poule (10-20 cm)');
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(5, 0.20, 0.50, 'Affaissements profonds (20-50 cm)');
INSERT INTO INTERVALLE_PROFONDEUR (id, profondeur_min, profondeur_max, description) VALUES
(6, 0.50, 2.00, 'Effondrements (>50 cm)');

DROP SEQUENCE SEQ_INTERVALLE_PROFONDEUR;
CREATE SEQUENCE SEQ_INTERVALLE_PROFONDEUR START WITH 7 INCREMENT BY 1 NOCACHE;

-- ==========================================
-- INSERTION DES PRIX DE REPARATION
-- ==========================================
-- Béton - Différents intervalles pour le même matériau
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(1, 'BÉTON', 1, 35.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(2, 'BÉTON', 2, 48.50);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(3, 'BÉTON', 3, 72.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(4, 'BÉTON', 4, 120.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(5, 'BÉTON', 5, 185.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(6, 'BÉTON', 6, 300.00);

-- Goudron (Bitume)
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(7, 'GOUDRON', 1, 25.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(8, 'GOUDRON', 2, 38.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(9, 'GOUDRON', 3, 55.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(10, 'GOUDRON', 4, 85.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(11, 'GOUDRON', 5, 130.00);

-- Pavés
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(12, 'PAVÉ', 1, 75.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(13, 'PAVÉ', 2, 110.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(14, 'PAVÉ', 3, 160.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(15, 'PAVÉ', 4, 240.00);

DROP SEQUENCE SEQ_REPARATION;
CREATE SEQUENCE SEQ_REPARATION START WITH 16 INCREMENT BY 1 NOCACHE;

-- ==========================================
-- INSERTION DES DÉGÂTS RÉALISTES
-- ==========================================
-- Chemin 1: Tana-Sambaina (200 km)
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(1, 1, 45.2, 2.5, 0.03);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(2, 1, 78.5, 1.8, 0.08);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(3, 1, 120.3, 4.2, 0.15);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(4, 1, 155.7, 3.1, 0.25);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(5, 1, 180.1, 6.5, 0.04);

DROP SEQUENCE SEQ_DEGAT;
CREATE SEQUENCE SEQ_DEGAT START WITH 5 INCREMENT BY 1 NOCACHE;

COMMIT;