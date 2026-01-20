==================================================================

## 1. Points et chemins

Le monde est composé de **points** (lieux) reliés entre eux par des **chemins**.
Un chemin relie un point A à un point B, et peut être **bidirectionnel** ou **unidirectionnel**.

Exemple
Paris → Lyon via **Autoroute A6**
Paris → Dijon → Lyon via deux routes secondaires

Les points forment un **graphe** :

* Sommets = Points
* Arêtes = Chemins
* Interdiction implicite de boucles infinies (ex. Paris→Dijon→Paris→Dijon)

---

## 2. Véhicules

Chaque véhicule possède :

* Nom
* Type (voiture, camion, moto…)
* Vitesse maximale (km/h)
* Largeur (m)
* Longueur (m)

Tous les véhicules réagissent pareil aux dommages.

Exemple :

| Nom            | Type    | Vitesse max | Largeur | Longueur |
| -------------- | ------- | ----------- | ------- | -------- |
| Peugeot 308    | Voiture | 200 km/h    | 1,80 m  | 4,25 m   |
| Camion Renault | Camion  | 90 km/h     | 2,50 m  | 12 m     |

---

## 3. Chemins (routes)

Chaque chemin est défini par :

* Nom
* Point de départ
* Point d’arrivée
* Distance (km)
* Largeur (m)
* Direction (aller simple ou double sens)

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

* Peugeot 308 (1,80 m) peut passer sur A6 (7 m → 3,5 m → OK)
* Le camion (2,50 m) ne peut pas sur une route de 3 m (1,50 m → NON)

---

## 5. Dommages sur les routes

Un **dommage** est une section d’un chemin où la vitesse est réduite.

Il est défini par :

* Chemin concerné
* Position début (km)
* Position fin (km)
* Taux de réduction (ex. 40 %)

Règles :

* Plusieurs dommages possibles
* Ils **ne se chevauchent jamais**
* Tous les véhicules subissent le **même effet**

Effet sur une portion :

```
vitesse_effective = vitesse_vehicule * (1 - taux_reduction)
```

Exemple sur A6 (460 km) :

* Dommage 100 → 150 km
* Réduction 40%
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

* Trajet direct Paris → Lyon (A6) = ~3h50
* Trajet Paris → Dijon + Dijon → Lyon = beaucoup plus long

**Critère unique retenu :**
→ temps total minimal

Méthode future recommandée pour calcul :

* Construire un graphe pondéré
* Utiliser un algorithme de plus court chemin basé sur le temps
  (Dijkstra ou A* avec heuristique de distance)

---

## 8. Résumé d’un voyage

Pour décrire un voyage complet :

* Véhicule utilisé
* Liste ordonnée des chemins
* Temps total
* Distance totale optionnelle
* Contrainte largeur respectée vérifiée
* Sens de route respecté (unidirectionnel OK)

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

   * Un chemin est **une route complète**
   * Un dommage est **un sous-segment** d’un chemin
   * 1 route → 0..N dommages
   * Dommage n’existe jamais sans route
   * En base de données :

     * `route_id` = clé étrangère dans **dommage**

2. **Point vs Chemin**

   * Un point existe indépendamment
   * Un chemin relie 2 points
   * En base :

     * `chemin(debut_point_id, fin_point_id)`

3. **Direction**

   * Si double sens : 1 entrée suffit avec un flag
   * Si sens unique : l’entrée suffit pour imposer le sens

4. **Vitesse**

   * Route ne stocke pas de vitesse (par décision)
   * Seul le véhicule en possède une

5. **Calculs**

   * La route garde ses distances et dommages
   * Le véhicule apporte la vitesse au moment du calcul

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

* 1 route → plusieurs dommages
* 1 route → 1 ou 2 points selon le sens
* 1 voyage → calcul programmatique, non stocké

==================================================================

Les technologies utilisées seront : JavaSwing, Postgres (pour les infos concernant la voiture), sinon le reste des bases de données seront sur oracle.

Exemple de strucute du projet :

Pojet
├── controller
│   └── *.java <- tous ce qui sera fonction or model et listener
├── DAO
│   └── *.java <- tous ce qui sera acces bases qui ne seront pas necessairement dans le model
├── inc
│   └── *.java <- connexion aux bases de donnees
├── model
│   └── *.java <- tous les models et leurs acces bases directement si necessaire
└── view
    └── *.java <- tous ce qui sera affichage dans le swing

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
docker cp /home/liantsoa/Documents/Ratah/Vacance/sql/Oracle.sql oracle:/tmp/Oracle.sql
```

3. Exécute-le dans Oracle avec `sqlplus`

```
@/tmp/Oracle.sql
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

*   Chemin du dossier : `src/main/java/test/`
*   Nom du fichier : `TestRunner.java` (ou `MainTest.java`)

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





