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