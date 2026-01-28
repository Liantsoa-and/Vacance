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
