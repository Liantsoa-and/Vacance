-- ==========================================
-- SCRIPT DE TEST POUR LES BORNES KILOMÉTRIQUES
-- ==========================================
-- Ce script permet de tester la fonctionnalité des bornes kilométriques
-- Il suppose que vous avez déjà des chemins, matériaux et dégâts dans la base
-- 1. VÉRIFIER LES DONNÉES EXISTANTES
-- ==========================================
-- Voir tous les chemins
SELECT
    id,
    nom,
    distance_km
FROM
    CHEMIN
ORDER BY
    id;

-- Voir tous les dégâts (avec leur position)
SELECT
    d.id,
    d.chemin_id,
    ch.nom AS chemin_nom,
    d.point_km,
    d.surface_m2,
    d.profondeur_m
FROM
    DEGAT d
    INNER JOIN CHEMIN ch ON d.chemin_id = ch.id
ORDER BY
    d.chemin_id,
    d.point_km;

-- Voir les réparations validées
SELECT
    rd.id,
    d.chemin_id,
    d.point_km,
    m.nom AS materiau,
    rd.cout_reparation,
    rd.validee
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
    INNER JOIN MATERIAU m ON rd.materiau_id = m.id
ORDER BY
    d.chemin_id,
    d.point_km;

-- 2. INSÉRER DES BORNES DE TEST
-- ==========================================
-- Supposons que le chemin avec ID=1 existe
-- Insérer des bornes tous les 5 km sur ce chemin
DELETE FROM BORNE
WHERE
    chemin_id = 1;

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 0.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 5.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 10.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 15.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 20.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 25.0);

INSERT INTO
    BORNE (id, chemin_id, km)
VALUES
    (SEQ_BORNE.NEXTVAL, 1, 30.0);

-- Voir les bornes créées
SELECT
    *
FROM
    BORNE
WHERE
    chemin_id = 1
ORDER BY
    km;

COMMIT;

-- 3. TESTS DE REQUÊTES
-- ==========================================
-- TEST 1: Trouver tous les dégâts entre 5 km et 20 km sur le chemin 1
SELECT
    d.id,
    d.point_km,
    d.surface_m2,
    d.profondeur_m
FROM
    DEGAT d
WHERE
    d.chemin_id = 1
    AND d.point_km >= 5.0
    AND d.point_km <= 20.0
ORDER BY
    d.point_km;

-- TEST 2: Calculer le coût total des réparations validées entre 5 km et 20 km
SELECT
    d.point_km,
    m.nom AS materiau,
    rd.cout_reparation,
    rd.validee
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
    INNER JOIN MATERIAU m ON rd.materiau_id = m.id
WHERE
    d.chemin_id = 1
    AND d.point_km >= 5.0
    AND d.point_km <= 20.0
    AND rd.validee = 1
ORDER BY
    d.point_km;

-- TEST 3: Somme totale des coûts entre 5 km et 20 km
SELECT
    COUNT(*) AS nombre_degats,
    COALESCE(SUM(rd.cout_reparation), 0) AS cout_total
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
WHERE
    d.chemin_id = 1
    AND d.point_km >= 5.0
    AND d.point_km <= 20.0
    AND rd.validee = 1;

-- 4. EXEMPLE AVEC DIFFÉRENTES PLAGES
-- ==========================================
-- Entre 0 et 10 km
SELECT
    'Entre 0 et 10 km' AS plage,
    COUNT(*) AS nb_degats,
    COALESCE(SUM(rd.cout_reparation), 0) AS cout
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
WHERE
    d.chemin_id = 1
    AND d.point_km >= 0
    AND d.point_km <= 10
    AND rd.validee = 1
UNION ALL
-- Entre 10 et 20 km
SELECT
    'Entre 10 et 20 km',
    COUNT(*),
    COALESCE(SUM(rd.cout_reparation), 0)
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
WHERE
    d.chemin_id = 1
    AND d.point_km > 10
    AND d.point_km <= 20
    AND rd.validee = 1
UNION ALL
-- Entre 20 et 30 km
SELECT
    'Entre 20 et 30 km',
    COUNT(*),
    COALESCE(SUM(rd.cout_reparation), 0)
FROM
    REPARATION_DEGAT rd
    INNER JOIN DEGAT d ON rd.degat_id = d.id
WHERE
    d.chemin_id = 1
    AND d.point_km > 20
    AND d.point_km <= 30
    AND rd.validee = 1;

-- 5. VÉRIFICATION DES CONTRAINTES
-- ==========================================
-- Vérifier l'unicité : cette requête doit échouer si une borne existe déjà à ce km
-- INSERT INTO BORNE (id, chemin_id, km) VALUES (SEQ_BORNE.NEXTVAL, 1, 5.0);
-- ORA-00001: violation de contrainte unique
-- 6. STATISTIQUES PAR CHEMIN
-- ==========================================
SELECT
    ch.id AS chemin_id,
    ch.nom AS chemin_nom,
    COUNT(DISTINCT b.id) AS nombre_bornes,
    COUNT(DISTINCT d.id) AS nombre_degats,
    COALESCE(
        SUM(
            CASE
                WHEN rd.validee = 1 THEN rd.cout_reparation
                ELSE 0
            END
        ),
        0
    ) AS cout_total_reparations
FROM
    CHEMIN ch
    LEFT JOIN BORNE b ON b.chemin_id = ch.id
    LEFT JOIN DEGAT d ON d.chemin_id = ch.id
    LEFT JOIN REPARATION_DEGAT rd ON rd.degat_id = d.id
GROUP BY
    ch.id,
    ch.nom
ORDER BY
    ch.id;

-- 7. NETTOYAGE (optionnel)
-- ==========================================
-- Pour supprimer toutes les bornes de test
-- DELETE FROM BORNE WHERE chemin_id = 1;
-- COMMIT;