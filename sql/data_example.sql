-- ==========================================
-- DONNÉES D'EXEMPLE
-- ==========================================

-- Insertion des matériaux
INSERT INTO MATERIAU (id, nom, description) VALUES (SEQ_MATERIAU.NEXTVAL, 'BETON', 'Béton standard pour revêtement routier');
INSERT INTO MATERIAU (id, nom, description) VALUES (SEQ_MATERIAU.NEXTVAL, 'PAVE', 'Pavé autobloquant');
INSERT INTO MATERIAU (id, nom, description) VALUES (SEQ_MATERIAU.NEXTVAL, 'GOUDRON', 'Enrobé bitumineux');

-- Insertion des tarifs de réparation pour BETON
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 1, 0, 0.5, 50000, 'Revêtement léger béton [0-0.5m]');

INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 1, 0.5, 999999, 70000, 'Réfection profonde béton [0.5m+]');

-- Insertion des tarifs de réparation pour PAVE
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 2, 0, 0.3, 70000, 'Pavage standard [0-0.3m]');

INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 2, 0.3, 999999, 90000, 'Réfection profonde pavé [0.3m+]');

-- Insertion des tarifs de réparation pour GOUDRON
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 3, 0, 0.4, 45000, 'Enrobé léger [0-0.4m]');

INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description) 
VALUES (SEQ_REPARATION.NEXTVAL, 3, 0.4, 999999, 65000, 'Enrobé épais [0.4m+]');

-- Exemples de dégâts (à adapter selon vos chemins existants)
-- Décommenter et adapter les IDs de chemins selon votre base
-- INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m, materiau_id) 
-- VALUES (SEQ_DEGAT.NEXTVAL, 1, 5.5, 20.5, 0.3, 1);  -- Dégât léger béton

-- INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m, materiau_id) 
-- VALUES (SEQ_DEGAT.NEXTVAL, 1, 12.8, 35.2, 0.7, 1);  -- Dégât profond béton

-- INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m, materiau_id) 
-- VALUES (SEQ_DEGAT.NEXTVAL, 2, 3.2, 15.0, 0.2, 2);  -- Dégât léger pavé

COMMIT;