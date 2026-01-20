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

-- Gravier compacté (pour réparations rapides)
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(16, 'GRAVIER', 1, 15.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(17, 'GRAVIER', 2, 22.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(18, 'GRAVIER', 3, 35.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(19, 'GRAVIER', 4, 50.00);

-- Asphalte
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(20, 'ASPHALTE', 1, 30.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(21, 'ASPHALTE', 2, 45.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(22, 'ASPHALTE', 3, 65.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(23, 'ASPHALTE', 4, 95.00);
INSERT INTO REPARATION (id, type_materiau, intervalle_profondeur_id, prix_par_m2) VALUES
(24, 'ASPHALTE', 5, 140.00);

DROP SEQUENCE SEQ_REPARATION;
CREATE SEQUENCE SEQ_REPARATION START WITH 25 INCREMENT BY 1 NOCACHE;

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

-- Chemin 2: Sambaina-Antsirabe (120 km)
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(6, 2, 15.8, 1.2, 0.02);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(7, 2, 40.5, 3.8, 0.12);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(8, 2, 65.2, 2.1, 0.06);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(9, 2, 90.7, 5.4, 0.35);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(10, 2, 105.3, 2.3, 0.09);

-- Chemin 3: Tana-Ampefy (163 km)
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(11, 3, 25.4, 1.5, 0.01);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(12, 3, 60.8, 2.8, 0.07);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(13, 3, 95.2, 4.6, 0.18);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(14, 3, 125.7, 3.3, 0.05);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(15, 3, 145.9, 7.2, 0.28);

-- Chemin 4: Ampefy-Sambaina (88 km)
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(16, 4, 12.5, 1.1, 0.03);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(17, 4, 35.2, 2.4, 0.11);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(18, 4, 52.8, 3.7, 0.22);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(19, 4, 70.5, 2.9, 0.08);
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(20, 4, 82.1, 4.8, 0.14);

-- Quelques dégâts sévères
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(21, 1, 65.8, 12.5, 0.65);  -- Effondrement sur Tana-Sambaina
INSERT INTO DEGAT (id, chemin_id, point_km, surface_m2, profondeur_m) VALUES
(22, 2, 110.2, 8.3, 0.42);  -- Affaissement profond sur Sambaina-Antsirabe

DROP SEQUENCE SEQ_DEGAT;
CREATE SEQUENCE SEQ_DEGAT START WITH 23 INCREMENT BY 1 NOCACHE;

-- ==========================================
-- VALIDATION DES DONNEES
-- ==========================================
COMMIT;

-- ==========================================
-- QUERIES DE TEST POUR VERIFIER LES DONNEES
-- ==========================================

-- 1. Afficher le nombre de dégâts par chemin
SELECT 
    ch.nom AS chemin,
    COUNT(d.id) AS nombre_degats,
    SUM(d.surface_m2) AS surface_totale_m2,
    ROUND(AVG(d.profondeur_m * 100), 2) || ' cm' AS profondeur_moyenne_cm
FROM DEGAT d
JOIN CHEMIN ch ON d.chemin_id = ch.id
GROUP BY ch.id, ch.nom
ORDER BY ch.nom;

-- 2. Voir toutes les options de réparation pour le dégât 1
SELECT 
    degat_id,
    nom_chemin,
    point_km,
    surface_m2,
    profondeur_m * 100 || ' cm' AS profondeur_cm,
    type_materiau,
    prix_par_m2,
    cout_reparation,
    description_intervalle,
    statut_correspondance
FROM VUE_DEGAT_REPARATION
WHERE degat_id = 1
ORDER BY type_materiau, statut_correspondance DESC;

-- 3. Trouver les réparations compatibles pour chaque dégât
SELECT 
    d.id AS degat_id,
    ch.nom AS chemin,
    d.point_km,
    d.surface_m2,
    d.profondeur_m,
    r.type_materiau,
    r.prix_par_m2,
    d.surface_m2 * r.prix_par_m2 AS cout_total,
    ip.description AS categorie
FROM DEGAT d
JOIN CHEMIN ch ON d.chemin_id = ch.id
JOIN INTERVALLE_PROFONDEUR ip ON d.profondeur_m BETWEEN ip.profondeur_min AND ip.profondeur_max
JOIN REPARATION r ON ip.id = r.intervalle_profondeur_id
WHERE d.id <= 5
ORDER BY d.id, r.type_materiau;

-- 4. Comparaison des coûts par matériau pour un dégât spécifique (ex: dégât 3)
SELECT 
    r.type_materiau,
    r.prix_par_m2,
    d.surface_m2 * r.prix_par_m2 AS cout_total,
    ip.description AS applicable_pour
FROM DEGAT d
CROSS JOIN REPARATION r
JOIN INTERVALLE_PROFONDEUR ip ON r.intervalle_profondeur_id = ip.id
WHERE d.id = 3
  AND d.profondeur_m BETWEEN ip.profondeur_min AND ip.profondeur_max
ORDER BY cout_total;

-- 5. Matériaux disponibles par type de profondeur
SELECT 
    ip.description AS intervalle_profondeur,
    LISTAGG(r.type_materiau, ', ') WITHIN GROUP (ORDER BY r.type_materiau) AS materiaux_disponibles,
    COUNT(r.type_materiau) AS nombre_materiaux
FROM INTERVALLE_PROFONDEUR ip
LEFT JOIN REPARATION r ON ip.id = r.intervalle_profondeur_id
GROUP BY ip.id, ip.description, ip.profondeur_min
ORDER BY ip.profondeur_min;

-- 6. Dégâts les plus coûteux à réparer (estimation avec béton)
SELECT 
    d.id AS degat_id,
    ch.nom AS chemin,
    d.point_km,
    d.surface_m2,
    d.profondeur_m * 100 || ' cm' AS profondeur_cm,
    r.prix_par_m2 AS prix_beton_m2,
    d.surface_m2 * r.prix_par_m2 AS cout_beton_total
FROM DEGAT d
JOIN CHEMIN ch ON d.chemin_id = ch.id
JOIN INTERVALLE_PROFONDEUR ip ON d.profondeur_m BETWEEN ip.profondeur_min AND ip.profondeur_max
JOIN REPARATION r ON ip.id = r.intervalle_profondeur_id AND r.type_materiau = 'BÉTON'
ORDER BY cout_beton_total DESC;