# Examen DevSecOps : API Bibliothèque

API REST Spring Boot 3 (Java 17) de gestion de livres, livrée par un pipeline CI/CD DevSecOps complet :
Gitleaks → Build/Tests Maven → SonarCloud → Docker → Trivy → Docker Hub, plus du monitoring Prometheus/Grafana.

## L'application

| Méthode | URL | Description |
|---|---|---|
| GET | `/` | Message d'accueil |
| GET | `/api/livres` | Liste des livres |
| GET | `/api/livres/{id}` | Un livre (404 s'il n'existe pas) |
| POST | `/api/livres` | Ajouter un livre (JSON validé, 400 si invalide) |
| DELETE | `/api/livres/{id}` | Supprimer un livre |
| POST | `/api/livres/{id}/emprunter` | Emprunter (409 si déjà emprunté) |
| POST | `/api/livres/{id}/rendre` | Rendre (409 si déjà disponible) |
| GET | `/actuator/health`, `/actuator/prometheus` | Santé et métriques |

Exemple :
```powershell
curl.exe -X POST http://localhost:8080/api/livres -H "Content-Type: application/json" -d '{\"titre\":\"Candide\",\"auteur\":\"Voltaire\",\"annee\":1759}'
```

---

## Étape 1 : créer l'app et la lancer en local

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.20.101-hotspot"
mvn clean package
java -jar target/bibliotheque.jar
```
Ouvrir http://localhost:8080 puis http://localhost:8080/api/livres

## Étape 2 : image Docker

```powershell
docker build -t bibliotheque .
docker run -d --name bibliotheque -p 8080:8080 bibliotheque
```
Le Dockerfile utilise une image JRE Alpine légère, met à jour les paquets (moins de CVE), lance l'app avec un **utilisateur non-root** et déclare un **HEALTHCHECK**.

## Étape 3 : dépôt GitHub

1. Créer un dépôt vide sur GitHub, par exemple `examen-devsecops`.
2. Dans le dossier `examen` :
```powershell
git init
git add .
git commit -m "feat: create bibliotheque app"
git branch -M main
git remote add origin https://github.com/<utilisateur>/examen-devsecops.git
git push -u origin main
```

## Étape 4 : secrets GitHub

Dans *Settings → Secrets and variables → Actions*, ajouter :

| Secret | Valeur |
|---|---|
| `DOCKERHUB_USERNAME` | ton identifiant Docker Hub |
| `DOCKERHUB_TOKEN` | un *Access Token* Docker Hub (Account settings → Personal access tokens) |
| `SONAR_TOKEN` | token SonarCloud (My Account → Security) |
| `SONAR_ORGANIZATION` | clé de l'organisation SonarCloud |
| `SONAR_PROJECT_KEY` | clé du projet SonarCloud |

Côté SonarCloud : importer le dépôt, puis dans *Administration → Analysis Method* **désactiver "Automatic Analysis"**, sinon l'analyse lancée par le CI échoue.

## Étape 5 : pipeline CI/CD (`.github/workflows/ci.yml`)

À chaque push sur `main`, le pipeline exécute :

1. **Checkout** avec l'historique complet
2. **Gitleaks** : détection de secrets dans le code et l'historique (rapport `gitleaks-report`)
3. **JDK 17**, puis **compilation**
4. **`mvn verify`** : tests unitaires (JUnit + MockMvc) et couverture JaCoCo (rapport `test-reports`)
5. **SonarCloud** : analyse SAST, qualité du code et couverture
6. **Build Docker**
7. **Test du conteneur** : attente du health check, puis appel de `/` et `/api/livres`
8. **Trivy** : scan des CVE CRITICAL/HIGH de l'image (rapport `trivy-report` + tableau dans les logs)
9. **Push** sur Docker Hub (seulement sur push, pas sur pull request)

Les rapports se téléchargent dans l'onglet *Actions → run → Artifacts*.

### Démonstration Gitleaks (comme au TP)
Créer une branche, ajouter un faux secret dans un fichier (par exemple une fausse clé AWS `AKIA...`), pousser : le job Gitleaks échoue. Supprimer ensuite le fichier et la branche.
Attention : si le faux secret est poussé sur `main`, il reste dans l'historique et Gitleaks bloquera tous les runs suivants.

## Étape 6 : corriger les vulnérabilités

Lire le rapport Trivy et le tableau dans les logs. Pour corriger :
- monter la version de Spring Boot dans `pom.xml` (dépendances Java)
- changer ou mettre à jour l'image de base dans le `Dockerfile` (paquets OS)

Committer avec un message du type `fix: vulnerabilities`, puis vérifier le nouveau rapport.

## Étape 7 : monitoring Prometheus + Grafana

```powershell
mvn clean package
cd monitoring
docker compose up -d --build
```

| Service | URL |
|---|---|
| Application | http://localhost:8080 |
| Prometheus | http://localhost:9090 (Status → Targets : `bibliotheque` doit être **UP**) |
| Grafana | http://localhost:3001 (admin / admin, à changer au premier login) |

La source de données Prometheus est déjà configurée dans Grafana (`monitoring/grafana/datasource.yml`).
Pour un dashboard : *Dashboards → New → Import* avec l'ID **4701** (JVM Micrometer) ou **11378** (Spring Boot), ou des requêtes à la main :
- `http_server_requests_seconds_count{application="bibliotheque"}`
- `jvm_memory_used_bytes{application="bibliotheque"}`
- `process_cpu_usage`

Générer un peu de trafic pour voir les courbes bouger :
```powershell
1..50 | ForEach-Object { curl.exe -s http://localhost:8080/api/livres > $null }
```

Arrêter :
```powershell
docker compose down
```

## Structure

```
examen/
├── .github/workflows/ci.yml      pipeline DevSecOps
├── Dockerfile                    image sécurisée (non-root, healthcheck)
├── pom.xml                       Spring Boot 3.3, Java 17, JaCoCo
├── monitoring/                   Prometheus + Grafana + app
└── src/
    ├── main/java/com/examen/bibliotheque/
    │   ├── controller/           AccueilController, LivreController
    │   ├── model/Livre.java
    │   └── service/LivreService.java
    └── test/java/...             tests unitaires et d'intégration
```
