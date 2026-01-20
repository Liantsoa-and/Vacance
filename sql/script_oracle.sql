-- ==========================================
-- NETTOYAGE COMPLET
-- ==========================================
BEGIN
   EXECUTE IMMEDIATE 'DROP VIEW VUE_DEGAT_REPARATION';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE REPARATION CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE INTERVALLE_PROFONDEUR CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE DEGAT CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE DOMMAGE CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE PAUSE CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE CHEMIN CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP TABLE POINT CASCADE CONSTRAINTS';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_DOMMAGE';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_PAUSE';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_CHEMIN';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

BEGIN
   EXECUTE IMMEDIATE 'DROP SEQUENCE SEQ_POINT';
EXCEPTION
   WHEN OTHERS THEN NULL;
END;
/

-- ==========================================
-- CRÉATION DES SÉQUENCES
-- ==========================================
CREATE SEQUENCE SEQ_POINT START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_CHEMIN START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_DOMMAGE START WITH 1 INCREMENT BY 1 NOCACHE;
CREATE SEQUENCE SEQ_PAUSE START WITH 1 INCREMENT BY 1 NOCACHE;

-- ==========================================
-- CRÉATION DES TABLES
-- ==========================================

-- Table des Lieux
CREATE TABLE POINT (
    id NUMBER PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL
);

-- Table des Chemins (Routes)
CREATE TABLE CHEMIN (
    id NUMBER PRIMARY KEY,
    nom VARCHAR2(100) NOT NULL,
    point_debut_id NUMBER NOT NULL REFERENCES POINT(id),
    point_fin_id NUMBER NOT NULL REFERENCES POINT(id),
    distance_km NUMBER(10, 2) NOT NULL,
    largeur_m NUMBER(5, 2) NOT NULL,
    sens VARCHAR2(20) NOT NULL CHECK (sens IN ('SIMPLE', 'DOUBLE'))
);

-- Table des Dommages
CREATE TABLE DOMMAGE (
    id NUMBER PRIMARY KEY,
    chemin_id NUMBER NOT NULL REFERENCES CHEMIN(id),
    debut_km NUMBER(10, 2) NOT NULL,
    fin_km NUMBER(10, 2) NOT NULL,
    reduction_taux NUMBER(5, 2) NOT NULL,
    CHECK (debut_km < fin_km)
);

-- Table des Pauses
CREATE TABLE PAUSE (
    id NUMBER PRIMARY KEY,
    chemin_id NUMBER NOT NULL REFERENCES CHEMIN(id),
    point_km NUMBER(10, 2) NOT NULL,
    heure_debut DATE NOT NULL,
    heure_fin DATE NOT NULL,
    CHECK (heure_debut < heure_fin)
);

-- Table des Dégâts
CREATE TABLE DEGAT (
    id NUMBER PRIMARY KEY,
    chemin_id NUMBER NOT NULL REFERENCES CHEMIN(id),
    point_km NUMBER(10, 2) NOT NULL,
    surface_m2 NUMBER(10, 2) NOT NULL,
    profondeur_m NUMBER(10, 2) NOT NULL
);

-- Table des Intervalles de Profondeur
CREATE TABLE INTERVALLE_PROFONDEUR (
    id NUMBER PRIMARY KEY,
    profondeur_min NUMBER(10, 2) NOT NULL CHECK (profondeur_min >= 0),
    profondeur_max NUMBER(10, 2) NOT NULL,
    description VARCHAR2(200),
    CHECK (profondeur_max > profondeur_min)
);

-- Table des Réparations avec prix par intervalle
CREATE TABLE REPARATION (
    id NUMBER PRIMARY KEY,
    type_materiau VARCHAR2(100) NOT NULL,
    intervalle_profondeur_id NUMBER NOT NULL REFERENCES INTERVALLE_PROFONDEUR(id),
    prix_par_m2 NUMBER(10, 2) NOT NULL CHECK (prix_par_m2 > 0),
    UNIQUE (type_materiau, intervalle_profondeur_id)
);

-- ==========================================
-- VUE POUR LIER DYNAMIQUEMENT DÉGÂTS ET RÉPARATIONS
-- ==========================================

CREATE OR REPLACE VIEW VUE_DEGAT_REPARATION AS
SELECT 
    -- Informations sur le dégât
    d.id AS degat_id,
    d.chemin_id,
    ch.nom AS nom_chemin,
    d.point_km,
    d.surface_m2,
    d.profondeur_m,
    -- Informations sur l'intervalle de profondeur
    ip.id AS intervalle_id,
    ip.profondeur_min,
    ip.profondeur_max,
    ip.description AS description_intervalle,
    -- Informations sur la réparation
    r.id AS reparation_id,
    r.type_materiau,
    r.prix_par_m2,
    -- Calculs
    d.surface_m2 * r.prix_par_m2 AS cout_reparation,
    -- Informations supplémentaires
    CASE 
        WHEN d.profondeur_m BETWEEN ip.profondeur_min AND ip.profondeur_max 
        THEN 'DANS_INTERVALLE'
        ELSE 'HORS_INTERVALLE'
    END AS statut_correspondance
FROM DEGAT d
-- Jointure avec le chemin pour avoir le nom
INNER JOIN CHEMIN ch ON d.chemin_id = ch.id
-- Jointure CROSS avec tous les matériaux de réparation
CROSS JOIN (
    SELECT DISTINCT type_materiau 
    FROM REPARATION
) materiau
-- Jointure avec les réparations pour chaque matériau
INNER JOIN REPARATION r ON materiau.type_materiau = r.type_materiau
-- Jointure avec l'intervalle de profondeur
INNER JOIN INTERVALLE_PROFONDEUR ip ON r.intervalle_profondeur_id = ip.id
ORDER BY d.id, r.type_materiau, ip.profondeur_min;