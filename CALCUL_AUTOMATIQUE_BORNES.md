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
