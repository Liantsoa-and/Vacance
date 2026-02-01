-- ======================================================
-- INSERTION DES DONNÉES
-- ======================================================

-- Points
DELETE FROM POINT;
INSERT INTO POINT (id, nom) VALUES (1, 'Tananarive');
INSERT INTO POINT (id, nom) VALUES (2, 'Sambaina');
INSERT INTO POINT (id, nom) VALUES (3, 'Antsirabe');
INSERT INTO POINT (id, nom) VALUES (4, 'Ampefy');
DROP SEQUENCE SEQ_POINT;
CREATE SEQUENCE SEQ_POINT START WITH 5 INCREMENT BY 1 NOCACHE;

-- Chemins
DELETE FROM CHEMIN;
INSERT INTO CHEMIN VALUES (1, 'Tana-Sambaina', 1, 2, 165, 10, 'DOUBLE');
INSERT INTO CHEMIN VALUES (2, 'Sambaina-Antsirabe', 2, 3, 120, 10, 'DOUBLE');
INSERT INTO CHEMIN VALUES (3, 'Tana-Ampefy', 1, 4, 163, 10, 'DOUBLE');
INSERT INTO CHEMIN VALUES (4, 'Ampefy-Sambaina', 4, 2, 88, 10, 'DOUBLE');
DROP SEQUENCE SEQ_CHEMIN;
CREATE SEQUENCE SEQ_CHEMIN START WITH 5 INCREMENT BY 1 NOCACHE;

-- Matériaux
DELETE FROM MATERIAU;
INSERT INTO MATERIAU (id, nom, description) VALUES (1, 'beton', 'Matériau de construction en béton');
INSERT INTO MATERIAU (id, nom, description) VALUES (2, 'goudron', 'Revêtement en asphalte/goudron');
INSERT INTO MATERIAU (id, nom, description) VALUES (3, 'pave', 'Pavés en pierre ou béton');
DROP SEQUENCE SEQ_MATERIAU;
CREATE SEQUENCE SEQ_MATERIAU START WITH 4 INCREMENT BY 1 NOCACHE;

-- Réparations pour le béton
DELETE FROM REPARATION;
-- Béton: 0 à 0.2 : 10000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (1, 1, 0, 0.2, 10000, 'Réparation béton - faible profondeur');

-- Béton: 0.2 à 0.4 : 15000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (2, 1, 0.2, 0.4, 15000, 'Réparation béton - profondeur moyenne');

-- Béton: 0.4 à 0.6 : 20000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (3, 1, 0.4, 0.6, 20000, 'Réparation béton - profondeur importante');

-- Béton: 0.6 à 0.8 : 25000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (4, 1, 0.6, 0.8, 25000, 'Réparation béton - très profonde');

-- Goudron: 0 à 0.4 : 15000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (5, 2, 0, 0.4, 15000, 'Réparation goudron - faible à moyenne profondeur');

-- Goudron: 0.4 à 0.8 : 20000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (6, 2, 0.4, 0.8, 20000, 'Réparation goudron - profondeur importante');

-- Pavé: 0 à 0.2 : 5000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (7, 3, 0, 0.2, 5000, 'Réparation pavé - faible profondeur');

-- Pavé: 0.2 à 0.5 : 6000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (8, 3, 0.2, 0.5, 6000, 'Réparation pavé - profondeur moyenne');

-- Pavé: 0.5 à 0.9 : 10000
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (9, 3, 0.5, 0.9, 10000, 'Réparation pavé - profondeur importante');

DROP SEQUENCE SEQ_REPARATION;
CREATE SEQUENCE SEQ_REPARATION START WITH 10 INCREMENT BY 1 NOCACHE;

-- Dégâts
DELETE FROM DEGAT;
-- Dégât 1 : chemin 1, point km 10, surface 10m2, profondeur 0.5m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (1, 1, 10, 10, 0.5);

-- Dégât 2 : chemin 1, point km 25, surface 5m2, profondeur 0.45m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (2, 1, 25, 5, 0.45);

-- Dégât 3 : chemin 1, point km 32, surface 12m2, profondeur 0.6m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (3, 1, 32, 12, 0.6);

-- Dégât 4 : chemin 1, point km 44, surface 3m2, profondeur 0.3m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (4, 1, 44, 3, 0.3);

-- Dégât 5 : chemin 1, point km 60, surface 30m2, profondeur 0.7m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (5, 1, 60, 30, 0.7);

-- Dégât 6 : chemin 1, point km 70, surface 10m2, profondeur 0.2m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (6, 1, 70, 10, 0.2);

-- Dégât 7 : chemin 1, point km 101, surface 7m2, profondeur 0.6m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (7, 1, 101, 7, 0.6);

-- Dégât 8 : chemin 1, point km 105, surface 42m2, profondeur 0.5m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (8, 1, 105, 42, 0.5);

-- Dégât 9 : chemin 1, point km 130, surface 11m2, profondeur 0.1m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (9, 1, 130, 11, 0.1);

-- Dégât 10 : chemin 1, point km 132, surface 25m2, profondeur 0.3m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (10, 1, 132, 25, 0.3);

-- Dégât 11 : chemin 1, point km 150, surface 13m2, profondeur 0.4m
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m)
VALUES (11, 1, 150, 13, 0.4);

DROP SEQUENCE SEQ_DEGAT;
CREATE SEQUENCE SEQ_DEGAT START WITH 12 INCREMENT BY 1 NOCACHE;

-- Dommages
DELETE FROM DOMMAGE;
-- 1 dommage
INSERT INTO DOMMAGE VALUES (1, 1, 30, 77, 0.22);
INSERT INTO DOMMAGE VALUES (2, 2, 12, 25, 0.66);
INSERT INTO DOMMAGE VALUES (3, 2, 56, 70, 0.11);
INSERT INTO DOMMAGE VALUES (4, 4, 25, 32, 0.15);

-- INSERT INTO DOMMAGE VALUES (2, 4, 50, 80, 0.25);

DROP SEQUENCE SEQ_DOMMAGE;
CREATE SEQUENCE SEQ_DOMMAGE START WITH 5 INCREMENT BY 1 NOCACHE;

-- Pauses (exemples)
-- DELETE FROM PAUSE;

-- Format : 'HH24:MI' signifie Heures (0-23) et Minutes
-- INSERT INTO PAUSE VALUES (1, 1, 100, TO_DATE('08:15', 'HH24:MI'), TO_DATE('09:30', 'HH24:MI')); 
-- INSERT INTO PAUSE VALUES (2, 2, 60, TO_DATE('12:00', 'HH24:MI'), TO_DATE('13:00', 'HH24:MI')); 

-- DROP SEQUENCE SEQ_PAUSE;
-- CREATE SEQUENCE SEQ_PAUSE START WITH 3 INCREMENT BY 1 NOCACHE;

-- Précipitations
DELETE FROM PRECIPITATION;
-- Précipitation 1 : chemin 1, 0-20 km, niveau 0.07 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (1, 1, 0, 20, 0.07);

-- Précipitation 2 : chemin 1, 21-30 km, niveau 0.01 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (2, 1, 21, 30, 0.01);

-- Précipitation 3 : chemin 1, 31-40 km, niveau 0.04 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (3, 1, 31, 40, 0.04);

-- Précipitation 4 : chemin 1, 41-50 km, niveau 0.08 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (4, 1, 41, 50, 0.08);

-- Précipitation 5 : chemin 1, 51-65 km, niveau 0.01 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (5, 1, 51, 65, 0.01);

-- Précipitation 6 : chemin 1, 66-80 km, niveau 0.08 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (6, 1, 66, 80, 0.08);

-- Précipitation 7 : chemin 1, 81-100 km, niveau 0.01 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (7, 1, 81, 100, 0.01);

-- Précipitation 8 : chemin 1, 101-120 km, niveau 0.06 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (8, 1, 101, 120, 0.06);

-- Précipitation 9 : chemin 1, 121-130 km, niveau 0.03 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (9, 1, 121, 130, 0.03);

-- Précipitation 10 : chemin 1, 131-150 km, niveau 0.01 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (10, 1, 131, 150, 0.01);

-- Précipitation 11 : chemin 1, 151-165 km, niveau 0.07 mm
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (11, 1, 151, 165, 0.07);

DROP SEQUENCE SEQ_PRECIPITATION;
CREATE SEQUENCE SEQ_PRECIPITATION START WITH 12 INCREMENT BY 1 NOCACHE;

DELETE FROM MATERIAU_PRECIPITATION;
-- Matériau-Précipitation associations pour chemin -- ...existing code...
INSERT INTO MATERIAU_PRECIPITATION (id, materiau_id, niveau_min_mm, niveau_max_mm) VALUES (1, 2, 0, 0.02);
INSERT INTO MATERIAU_PRECIPITATION (id, materiau_id, niveau_min_mm, niveau_max_mm) VALUES (2, 3, 0.03, 0.06);
INSERT INTO MATERIAU_PRECIPITATION (id, materiau_id, niveau_min_mm, niveau_max_mm) VALUES (3, 1, 0.06, 0.09);

DROP SEQUENCE SEQ_MATERIAU_PRECIPITATION;
CREATE SEQUENCE SEQ_MATERIAU_PRECIPITATION START WITH 4 INCREMENT BY 1 NOCACHE;

COMMIT;