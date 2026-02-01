==================================================================

## 1. Points et chemins

Le monde est composé de **points** (lieux) reliés entre eux par des **chemins**.
Un chemin relie un point A à un point B, et peut être **bidirectionnel** ou **unidirectionnel**.

Exemple
Paris → Lyon via **Autoroute A6**
Paris → Dijon → Lyon via deux routes secondaires

Les points forment un **graphe** :

- Sommets = Points
- Arêtes = Chemins
- Interdiction implicite de boucles infinies (ex. Paris→Dijon→Paris→Dijon)

---

## 2. Véhicules

Chaque véhicule possède :

- Nom
- Type (voiture, camion, moto…)
- Vitesse maximale (km/h)
- Largeur (m)
- Longueur (m)

Tous les véhicules réagissent pareil aux dommages.

Exemple :

| Nom            | Type    | Vitesse max | Largeur | Longueur |
| -------------- | ------- | ----------- | ------- | -------- |
| Peugeot 308    | Voiture | 200 km/h    | 1,80 m  | 4,25 m   |
| Camion Renault | Camion  | 90 km/h     | 2,50 m  | 12 m     |

---

## 3. Chemins (routes)

Chaque chemin est défini par :

- Nom
- Point de départ
- Point d’arrivée
- Distance (km)
- Largeur (m)
- Direction (aller simple ou double sens)

Une route peut avoir plusieurs zones endommagées **qui ne se chevauchent jamais**.

Exemple :

| Nom                  | De    | Vers  | Distance | Largeur | Direction    |
| -------------------- | ----- | ----- | -------- | ------- | ------------ |
| Autoroute A6         | Paris | Lyon  | 460 km   | 7 m     | Double sens  |
| Route Départementale | Paris | Dijon | 310 km   | 3,5 m   | Double sens  |
| Route de campagne    | Dijon | Lyon  | 200 km   | 3 m     | Aller simple |

---

## 4. Règle de passage par largeur

Un véhicule peut emprunter un chemin **si et seulement si**

```
largeur_vehicule < largeur_chemin / 2
```

Les données sont modifiables et pourront être réalignées plus tard.

Exemples :

- Peugeot 308 (1,80 m) peut passer sur A6 (7 m → 3,5 m → OK)
- Le camion (2,50 m) ne peut pas sur une route de 3 m (1,50 m → NON)

---

## 5. Dommages sur les routes

Un **dommage** est une section d’un chemin où la vitesse est réduite.

Il est défini par :

- Chemin concerné
- Position début (km)
- Position fin (km)
- Taux de réduction (ex. 40 %)

Règles :

- Plusieurs dommages possibles
- Ils **ne se chevauchent jamais**
- Tous les véhicules subissent le **même effet**

Effet sur une portion :

```
vitesse_effective = vitesse_vehicule * (1 - taux_reduction)
```

Exemple sur A6 (460 km) :

- Dommage 100 → 150 km
- Réduction 40%
  → Sur 50 km : vitesse = 60% de la vitesse normale

---

## 6. Temps de trajet d’un chemin

Le temps total est la somme des durées par portion :

```
temps_total = Σ (distance_portion / vitesse_portion)
```

Pour un chemin sans dommages :

```
temps = distance_totale / vitesse_vehicule
```

Exemple simplifié :
Voiture Peugeot 308, A6 = 460 km, vitesse estimée = 120 km/h
Temps = 460 / 120 ≈ 3,83 h → 3 h 50 min

---

## 7. Trajet multi-chemins et meilleur chemin

Pour aller d’un point A à un point C, on peut emprunter plusieurs chemins intermédiaires.

Exemple :

- Trajet direct Paris → Lyon (A6) = ~3h50
- Trajet Paris → Dijon + Dijon → Lyon = beaucoup plus long

**Critère unique retenu :**
→ temps total minimal

Méthode future recommandée pour calcul :

- Construire un graphe pondéré
- Utiliser un algorithme de plus court chemin basé sur le temps
  (Dijkstra ou A\* avec heuristique de distance)

---

## 8. Résumé d’un voyage

Pour décrire un voyage complet :

- Véhicule utilisé
- Liste ordonnée des chemins
- Temps total
- Distance totale optionnelle
- Contrainte largeur respectée vérifiée
- Sens de route respecté (unidirectionnel OK)

Exemple final :

Voyage : Paris → Lyon
Véhicule : Peugeot 308
Itinéraire : Autoroute A6
Distance : 460 km
Temps estimé : 3 h 50 min
Conforme : oui

==================================================================

Points qui se confondent dans la logique (à clarifier pour la base)

1. **Chemin vs Dommage**
   - Un chemin est **une route complète**
   - Un dommage est **un sous-segment** d’un chemin
   - 1 route → 0..N dommages
   - Dommage n’existe jamais sans route
   - En base de données :
     - `route_id` = clé étrangère dans **dommage**

2. **Point vs Chemin**
   - Un point existe indépendamment
   - Un chemin relie 2 points
   - En base :
     - `chemin(debut_point_id, fin_point_id)`

3. **Direction**
   - Si double sens : 1 entrée suffit avec un flag
   - Si sens unique : l’entrée suffit pour imposer le sens

4. **Vitesse**
   - Route ne stocke pas de vitesse (par décision)
   - Seul le véhicule en possède une

5. **Calculs**
   - La route garde ses distances et dommages
   - Le véhicule apporte la vitesse au moment du calcul

==================================================================

## Schéma base de données minimal cohérent

```
Point(id, nom)

Chemin(id, nom, point_debut_id, point_fin_id, distance_km, largeur_m, sens)
  sens = {SIMPLE, DOUBLE}

Dommage(id, chemin_id, debut_km, fin_km, reduction_taux)

Vehicule(id, nom, type, vitesse_max, largeur_m, longueur_m)
```

Relations :

- 1 route → plusieurs dommages
- 1 route → 1 ou 2 points selon le sens
- 1 voyage → calcul programmatique, non stocké

==================================================================

Les technologies utilisées seront : JavaSwing, Postgres (pour les infos concernant la voiture), sinon le reste des bases de données seront sur oracle.

Exemple de strucute du projet :

Pojet
├── controller
│   └── _.java <- tous ce qui sera fonction or model et listener
├── DAO
│   └── _.java <- tous ce qui sera acces bases qui ne seront pas necessairement dans le model
├── inc
│   └── _.java <- connexion aux bases de donnees
├── model
│   └── _.java <- tous les models et leurs acces bases directement si necessaire
└── view
└── \*.java <- tous ce qui sera affichage dans le swing

==================================================================

Comment executer un fichier sql dans oracle qui est un conteneur :
Méthode la plus simple : `docker exec` + `sqlplus`.

1. Vérifie le nom du conteneur

```
docker ps
```

2. Si ton fichier est **sur ta machine (host)**, copie-le dans le conteneur

```
docker cp chemin/vers/script.sql nom_du_conteneur:/tmp/script.sql
```

3. Exécute-le dans Oracle avec `sqlplus`

```
@/tmp/script.sql
```

---

### Variante rapide si ton fichier est déjà monté dans le conteneur

(Par exemple via un volume `/data`)

```
docker exec -it nom_du_conteneur sqlplus user/password@//localhost:1521/XEPDB1 @/data/script.sql
```

---

### Pour vérifier que tout marche

Dans le conteneur :

```
docker exec -it nom_du_conteneur bash
sqlplus user/password@//localhost:1521/XEPDB1
```

Puis tu peux faire :

```
@/tmp/script.sql
```

==================================================================

### 1. Où le mettre ?

Je te conseille de créer un nouveau dossier (package) spécifique pour garder tes tests séparés de la logique métier et de l'interface.

- Chemin du dossier : `src/main/java/test/`
- Nom du fichier : `TestRunner.java` (ou `MainTest.java`)

Ta structure ressemblera donc à ceci :

```text
src/main/java/
├── controller/
├── DAO/
├── inc/
├── model/
├── test/
│   └── TestRunner.java   <-- Ton fichier de tests
└── view/
```

### 2. À quoi ressemble le fichier ?

C'est un fichier Java standard. L'idée est d'y mettre des petites fonctions de test que tu appelles depuis le `main`. Tu mets tout en commentaires au début, et tu décommentes au fur et à mesure que tu avances dans le développement.

### 3. Comment l'exécuter ?

Puisque tu as un script `start.sh`, tu as deux options :

**Lancer directement via une commande manuelle**
Ouvre ton terminal, va à la racine du projet et tape :

```bash
# Compilation (ton script le fait déjà, mais si tu veux juste compiler le test)
javac -d bin -cp "lib/*:src/main/java" src/main/java/test/MainTest.java

# Exécution
java -cp "bin:lib/*" test.MainTest
```

==================================================================

## Évolution du système - 27/01/26

### Gestion automatique des matériaux par précipitations

**Concept :** Le système évolue pour automatiser le choix des matériaux de réparation en fonction des niveaux de précipitations sur les chemins, plutôt que de laisser l'utilisateur choisir manuellement.

#### 1. Nouvelles tables

**Table PRECIPITATION :**

```sql
CREATE TABLE PRECIPITATION (
    id NUMBER PRIMARY KEY,
    chemin_id NUMBER NOT NULL REFERENCES CHEMIN(id),
    debut_km NUMBER(8,3) NOT NULL,
    fin_km NUMBER(8,3) NOT NULL,
    niveau_mm NUMBER(8,3) NOT NULL
);
```

**Table MATERIAU_PRECIPITATION :**

```sql
CREATE TABLE MATERIAU_PRECIPITATION (
    id NUMBER PRIMARY KEY,
    materiau_id NUMBER NOT NULL REFERENCES MATERIAU(id),
    niveau_min_mm NUMBER(8,3) NOT NULL,
    niveau_max_mm NUMBER(8,3) NOT NULL
);
```

#### 2. Règles de fonctionnement

**Configuration par intervalles :**

- Chaque matériau est associé à un intervalle de précipitations ]min; max]
- Exemple : 0 → 0.2mm (béton), 0.2 → 0.5mm (pavé), etc.
- Configuration personnalisable via formulaire

**Sélection automatique :**

- Lors d'une réparation, le système détermine automatiquement le matériau
- Basé sur le niveau de précipitation au point kilométrique du dégât
- Plus de choix manuel des matériaux

#### 3. Gestion des zones de précipitations

**Principe :**

- Les précipitations sont définies par intervalles kilométriques sur chaque chemin
- Similaire aux dommages : début_km → fin_km avec niveau en mm
- Un dégât (point précis) appartient à UNE SEULE zone

**Règle de non-chevauchement :**

- Les zones de précipitations ne peuvent pas se chevaucher
- Entre deux zones = 0mm de précipitation (zone neutre)
- Exemple valide : Zone A (2km→4km), Zone B (6km→8km)
- Exemple invalide : Zone A (2km→6km), Zone B (4km→8km)

#### 4. Logique de sélection

**Pour un dégât au point kilométrique X :**

1. Chercher la zone où `debut_km <= X < fin_km`
2. Si zone trouvée → utiliser son niveau de précipitation
3. Si aucune zone → considérer 0mm (matériau par défaut)
4. Trouver le matériau où `niveau_min_mm < précipitation <= niveau_max_mm`
5. Calculer le coût avec ce matériau

**Cas particuliers à gérer :**

- Aucune zone de précipitation définie sur un chemin
  => La zone de précipitation sur ce chemin sera de 0mm
- Aucun matériau ne correspond au niveau trouvé
- Gaps entre les zones (matériau par défaut)

#### 5. Impact sur l'existant

**Classes à créer :**

- `Precipitation.java` (model)
- `MateriauPrecipitation.java` (model)
- `PrecipitationDAO.java` (dao)
- `MateriauPrecipitationDAO.java` (dao)

**Classes à modifier :**

- `ReparationDegatDAO.java` : Intégrer la logique de sélection automatique
- Interface de gestion des réparations : Remplacer sélection manuelle

**Validations nécessaires :**

- Vérifier la non-superposition des zones lors de la saisie
- S'assurer de la couverture complète des intervalles de matériaux
- Gérer les cas d'exception avec des matériaux par défaut

==================================================================

# 📍 Fonctionnalité : Bornes Kilométriques

## Vue d'ensemble

Cette fonctionnalité permet de calculer le coût total des réparations entre deux bornes kilométriques sur un chemin donné.

## Concept

Le système fonctionne selon le principe suivant :

**Exemple :**

- **Dégâts sur le chemin** : aux positions 10, 25, 32, 44, 60 km
- **Bornes sélectionnées** : Début = 9 km, Fin = 43 km
- **Dégâts inclus dans le calcul** : 10, 25, 32 km
- **Résultat** : Le système calcule uniquement le coût de réparation pour les dégâts situés entre 9 km et 43 km

## Base de données

### Table BORNE

```sql
CREATE TABLE BORNE (
    id NUMBER PRIMARY KEY,
    chemin_id NUMBER NOT NULL REFERENCES CHEMIN(id),
    km NUMBER(10, 2) NOT NULL,
    UNIQUE (chemin_id, km)
);
```

**Champs :**

- `id` : Identifiant unique de la borne
- `chemin_id` : Référence vers le chemin
- `km` : Position kilométrique de la borne sur le chemin

## Architecture

### 1. Modèle

**`model/Borne.java`**

- Classe représentant une borne kilométrique
- Attributs : `id`, `cheminId`, `km`

### 2. DAO (Data Access Object)

**`dao/BorneDAO.java`**

- `getAll()` : Récupère toutes les bornes
- `getBornesByCheminId(int cheminId)` : Bornes d'un chemin spécifique
- `getById(int id)` : Récupère une borne par son ID
- `insert(Borne)` : Ajoute une nouvelle borne
- `update(Borne)` : Met à jour une borne
- `delete(int id)` : Supprime une borne

**`dao/DegatDAO.java`** (méthodes ajoutées)

- `getDegatsByCheminId(int cheminId)` : Tous les dégâts d'un chemin
- `getDegatsBetweenBornes(int cheminId, double kmDebut, double kmFin)` : Filtre les dégâts entre deux bornes

**`dao/ReparationDegatDAO.java`** (méthode ajoutée)

- `getByDegatId(int degatId)` : Récupère les réparations d'un dégât spécifique

### 3. Contrôleur

**`controller/BorneController.java`**

**Méthodes principales :**

- `getAllDegatsByCheminId(int cheminId)` : Liste complète des dégâts d'un chemin
- `getDegatsBetweenBornes(int cheminId, double kmDebut, double kmFin)` : Dégâts filtrés entre deux bornes
- `calculerPrixReparationBetweenBornes(int cheminId, double kmDebut, double kmFin)` : Calcule le coût total
- `calculerReparationComplete(int cheminId, double kmDebut, double kmFin)` : Retourne un objet détaillé avec tous les résultats

**Classe interne `ResultatReparationBornes` :**

```java
- double kmDebut
- double kmFin
- List<Degat> degats
- double coutTotal
- int nombreDegats
```

### 4. Interface graphique

**`view/BorneFilterPanel.java`**

**Fonctionnalités :**

1. **Sélection de zone**
   - Choix du chemin
   - Saisie de la borne de début (km)
   - Saisie de la borne de fin (km)

2. **Affichage des résultats**
   - Informations sur la zone analysée
   - Nombre de dégâts trouvés
   - **Coût total en grand format**
   - **Table détaillée** avec :
     - Numéro du dégât
     - Position kilométrique
     - Surface (m²)
     - Profondeur (m)
     - Matériau utilisé
     - Coût de réparation
     - Statut de validation (✓ Oui / ✗ Non)

3. **Gestion des bornes**
   - Bouton "Gérer les Bornes" ouvre un dialogue
   - Ajout de nouvelles bornes
   - Suppression de bornes existantes
   - Liste des bornes par chemin

## Intégration dans l'application

Le panel est ajouté comme un nouvel onglet dans `MainFrame.java` :

```java
BorneFilterPanel panelBornes = new BorneFilterPanel();
tabbedPane.addTab("📍 Bornes Kilométriques", panelBornes);
```

## Logique de calcul

Le calcul suit la même logique que les précipitations :

1. **Filtrage spatial** : Seuls les dégâts dont la position (`point_km`) est située entre `kmDebut` et `kmFin` (inclus) sont considérés

2. **Calcul du coût** : Pour chaque dégât filtré :
   - Récupérer toutes les réparations associées
   - **Compter uniquement les réparations validées** (`validee = 1`)
   - Additionner les coûts de réparation

3. **SQL utilisé** :

```sql
SELECT * FROM DEGAT
WHERE chemin_id = ?
  AND point_km >= ?
  AND point_km <= ?
ORDER BY point_km
```

## Utilisation

### 1. Via l'interface graphique

1. Ouvrir l'onglet **"📍 Bornes Kilométriques"**
2. Sélectionner un chemin
3. Saisir la borne de début (ex: 9.0)
4. Saisir la borne de fin (ex: 43.0)
5. Cliquer sur **"🔍 Calculer"**
6. Consulter les résultats dans la table

### 2. Gestion des bornes

1. Sélectionner un chemin
2. Cliquer sur **"⚙ Gérer les Bornes"**
3. Dans le dialogue :
   - Voir toutes les bornes du chemin
   - Ajouter une nouvelle borne (saisir le km)
   - Supprimer une borne existante

### 3. Programmation

```java
BorneController controller = new BorneController();

// Récupérer tous les dégâts d'un chemin
List<Degat> tousLesDegats = controller.getAllDegatsByCheminId(1);

// Filtrer entre deux bornes
List<Degat> degatsFiltre = controller.getDegatsBetweenBornes(1, 9.0, 43.0);

// Calculer le coût
double cout = controller.calculerPrixReparationBetweenBornes(1, 9.0, 43.0);

// Obtenir un résultat complet
ResultatReparationBornes resultat = controller.calculerReparationComplete(1, 9.0, 43.0);
System.out.println("Nombre de dégâts : " + resultat.getNombreDegats());
System.out.println("Coût total : " + resultat.getCoutTotal() + " €");
```

## Exemple de données

Voir le fichier `sql/exemple_bornes.sql` pour des exemples d'insertion de bornes.

## Points importants

1. **Réparations validées uniquement** : Seules les réparations avec `validee = 1` sont comptabilisées dans le coût total

2. **Bornes uniques** : Une contrainte UNIQUE sur `(chemin_id, km)` empêche d'avoir deux bornes au même kilomètre sur un chemin

3. **Filtrage inclusif** : Les dégâts situés exactement sur les bornes de début ou de fin sont inclus dans le calcul

4. **Interface claire** : Le tableau affiche toutes les réparations (validées et non validées) avec leur statut, mais seules les validées sont comptées dans le total

## Modifications apportées

### Fichiers créés

- `src/main/java/model/Borne.java`
- `src/main/java/dao/BorneDAO.java`
- `src/main/java/controller/BorneController.java`
- `src/main/java/view/BorneFilterPanel.java`
- `sql/exemple_bornes.sql`

### Fichiers modifiés

- `sql/script_oracle.sql` : Ajout de la table BORNE et de la séquence
- `src/main/java/dao/DegatDAO.java` : Ajout des méthodes de filtrage
- `src/main/java/dao/ReparationDegatDAO.java` : Ajout de `getByDegatId()`
- `src/main/java/model/ReparationDegat.java` : Changement de `double` vers `Double` pour `coutReparation`
- `src/main/java/view/MainFrame.java` : Ajout du nouvel onglet

# 🔄 MISE À JOUR : Calcul Automatique avec Précipitations

## Changements apportés

Le système de calcul des réparations entre bornes kilométriques a été mis à jour pour **suivre la logique des précipitations**.

## Nouvelle logique de calcul

### Avant ❌

- Le système cherchait les réparations déjà validées en base de données
- Il fallait d'abord créer et valider les réparations manuellement
- Seules les réparations validées étaient comptabilisées

### Maintenant ✅

- **Calcul automatique et immédiat** dès qu'on clique sur "Calculer"
- **Sélection automatique du matériau** selon le niveau de précipitation à la position du dégât
- **Calcul du coût** basé sur : `Surface (m²) × Prix au m² du matériau`
- **Pas besoin de validation préalable** : le système calcule directement

## Processus de calcul détaillé

Pour chaque dégât entre les deux bornes :

1. **Localisation** : Récupérer la position kilométrique du dégât
2. **Analyse des précipitations** : Détecter le niveau de précipitation à cette position
3. **Sélection du matériau** : Choisir automatiquement le matériau approprié selon les précipitations
4. **Recherche de la réparation** : Trouver la réparation correspondant à la profondeur du dégât
5. **Calcul du coût** : `Surface du dégât × Prix au m² de la réparation`
6. **Totalisation** : Additionner tous les coûts

## Exemple concret

### Données

- **Chemin** : Route Nationale 1
- **Bornes** : 10 km → 30 km
- **Dégâts trouvés** :
  - Position 15 km : 50 m², profondeur 0.15 m, précipitation 20 mm
  - Position 22 km : 30 m², profondeur 0.25 m, précipitation 50 mm
  - Position 28 km : 40 m², profondeur 0.10 m, précipitation 10 mm

### Calcul automatique

1. **Dégât à 15 km** :
   - Précipitation : 20 mm → Matériau : Bitume standard
   - Profondeur 0.15 m → Réparation : "Reprise superficielle" à 5000 Ar/m²
   - Coût : 50 m² × 5000 Ar = 250 000 Ar

2. **Dégât à 22 km** :
   - Précipitation : 50 mm → Matériau : Bitume renforcé
   - Profondeur 0.25 m → Réparation : "Reprise profonde" à 8000 Ar/m²
   - Coût : 30 m² × 8000 Ar = 240 000 Ar

3. **Dégât à 28 km** :
   - Précipitation : 10 mm → Matériau : Bitume standard
   - Profondeur 0.10 m → Réparation : "Reprise légère" à 3000 Ar/m²
   - Coût : 40 m² × 3000 Ar = 120 000 Ar

**TOTAL : 610 000 Ar**

## Interface utilisateur

### Modifications apportées

1. **Affichage immédiat du prix total** en gros format
2. **Table détaillée** avec :
   - N° : Numéro séquentiel du dégât
   - Position (km) : Localisation sur le chemin
   - Surface (m²) : Surface endommagée
   - Profondeur (m) : Profondeur du dégât
   - Matériau : Matériau **sélectionné automatiquement**
   - Coût (Ar) : Coût calculé pour ce dégât
   - Description : Type de réparation appliqué

3. **Message de confirmation** expliquant que le matériau a été sélectionné automatiquement

## Code modifié

### BorneController.java

```java
public double calculerPrixReparationBetweenBornes(int cheminId, double kmDebut, double kmFin) {
    List<Degat> degats = getDegatsBetweenBornes(cheminId, kmDebut, kmFin);
    double coutTotal = 0.0;

    for (Degat degat : degats) {
        // 1. Sélection automatique du matériau selon les précipitations
        Materiau materiau = materialService.getMaterialForDommage(
            degat.getCheminId(),
            degat.getPointKm());

        // 2. Trouver la réparation appropriée selon la profondeur
        Reparation reparation = reparationDAO.findByMateriauAndProfondeur(
            materiau.getId(),
            degat.getProfondeurM());

        if (reparation != null) {
            // 3. Calculer : surface × prix au m²
            double cout = degat.getSurfaceM2() * reparation.getPrixParM2();
            coutTotal += cout;
        }
    }
    return coutTotal;
}
```

### Nouvelle classe interne : DegatAvecCout

Encapsule un dégât avec son coût calculé :

```java
public static class DegatAvecCout {
    private Degat degat;
    private Materiau materiau;
    private double coutReparation;
    private String descriptionReparation;
}
```

### ReparationDAO.java

Nouvelle méthode ajoutée :

```java
public Reparation findByMateriauAndProfondeur(int materiauId, double profondeur) {
    // Trouve la réparation où profondeur est dans l'intervalle ]min, max]
    String sql = "SELECT * FROM REPARATION " +
                 "WHERE materiau_id = ? " +
                 "AND ? > profondeur_min " +
                 "AND ? <= profondeur_max";
    // ...
}
```

## Avantages

✅ **Rapidité** : Calcul instantané, pas besoin de créer les réparations à l'avance  
✅ **Automatisation** : Sélection intelligente du matériau selon les conditions  
✅ **Cohérence** : Suit exactement la même logique que le reste du système  
✅ **Simplicité** : L'utilisateur entre juste les bornes et obtient le prix  
✅ **Flexibilité** : Recalcul facile si les données changent

## Prérequis pour le fonctionnement

Pour que le calcul fonctionne correctement, il faut avoir en base :

1. ✅ **Chemins** définis
2. ✅ **Dégâts** enregistrés avec leurs positions kilométriques
3. ✅ **Zones de précipitation** configurées (table PRECIPITATION)
4. ✅ **Relations matériau-précipitation** (table MATERIAU_PRECIPITATION)
5. ✅ **Matériaux** disponibles
6. ✅ **Tarifs de réparation** par matériau et profondeur (table REPARATION)

## Utilisation

1. Ouvrir l'onglet **"📍 Bornes Kilométriques"**
2. Sélectionner un chemin
3. Entrer la borne de début (ex: 10.0)
4. Entrer la borne de fin (ex: 30.0)
5. Cliquer sur **"🔍 Calculer"**
6. Le prix total s'affiche immédiatement ✓

Le système analyse automatiquement :

- Les dégâts entre ces deux points
- Les niveaux de précipitation à chaque position
- Les matériaux appropriés
- Les coûts de réparation correspondants

## Tests

Pour tester le système, assurez-vous d'avoir :

```sql
-- Des précipitations définies
INSERT INTO PRECIPITATION (id, chemin_id, debut_km, fin_km, niveau_mm)
VALUES (SEQ_PRECIPITATION.NEXTVAL, 1, 0, 20, 30.5);

-- Des relations matériau-précipitation
INSERT INTO MATERIAU_PRECIPITATION (id, materiau_id, niveau_min_mm, niveau_max_mm)
VALUES (SEQ_MATERIAU_PRECIPITATION.NEXTVAL, 1, 0, 50);

-- Des tarifs de réparation
INSERT INTO REPARATION (id, materiau_id, profondeur_min, profondeur_max, prix_par_m2, description)
VALUES (SEQ_REPARATION.NEXTVAL, 1, 0, 0.2, 5000, 'Reprise superficielle');
```

## Résumé

Le système calcule maintenant **automatiquement et immédiatement** le coût des réparations entre deux bornes, en utilisant la logique de sélection du matériau basée sur les précipitations. Plus besoin de validation manuelle : le prix est donné dès qu'on clique sur "Calculer" !

