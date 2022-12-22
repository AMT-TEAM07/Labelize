# Labelize : CLI Client

[![Verify & Package - CLI Client](https://github.com/AMT-TEAM07/Labelize/actions/workflows/cli-client-verify.yml/badge.svg)](https://github.com/AMT-TEAM07/Labelize/actions/workflows/cli-client-verify.yml) [![Deploy on Docker Hub - CLI Client](https://github.com/AMT-TEAM07/Labelize/actions/workflows/cli-client-deploy.yml/badge.svg)](https://github.com/AMT-TEAM07/Labelize/actions/workflows/cli-client-deploy.yml)

Ce micro-service fait parti du projet [Labelize](https://github.com/AMT-TEAM07/Labelize).

Ce projet communique avec les deux autres micro-services de l'application: `label-detection-service` et `label-detection-service` au travers de leur API REST.

Cette partie du projet a pour but de faire des tests d'intégration entre les micro-services via les différents scénarios décrits plus bas.

## Prérequis

- [Java 17 (LTS)](https://adoptium.net/temurin/releases)
- [Maven 3.8](https://maven.apache.org/download.cgi)

Optionnel mais fortement recommandé:

- [IntelliJ IDEA](https://www.jetbrains.com/fr-fr/idea/download/#section=windows)

## Mise en route

1. Créer le fichier .env avec les variables d'environnement.

```bash
# Copie du fichier .env.example
cp .env.example .env

# Remplissage des variables d'environnement
vi .env
```

2. Installer les dépendances

```bash
mvn clean install -DskipTests
```

3. Créer un exécutable JAR et lancer l'application

```bash
# Package l'application
mvn package -DskipTests

# Lancer l'application
java -jar target/*.jar
```

## Scénarios

Nous avons 3 scénarios de tests donnés par le mandant du projet. Chaque scénario est décrit dans les sections suivantes. Ces derniers sont exécutés dans l'ordre au lancement de l'application.

### Scénario 1

Dans ce scénario, rien n'existe. Nous allons donc créer le bucket et upload l'image `lausanne.jpg`.

Voici les différentes étapes:
* Le bucket doit être créé
* L’image doit être uploadée
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.

#### Résultat attendu
Nous devons obtenir un fichier `lausanne.jpg` ainsi qu'un fichier `lausanne.jpg.json` dans le bucket S3. Le fichier `lausanne.jpg.json` doit contenir le résultat de l'analyse de l'image.

### Scénario 2

Dans ce scénario, le bucket existe mais il est vide.

Voici les différentes étapes:
* L’image doit être uploadée
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.

#### Résultat attendu
Nous devons obtenir un fichier `montreux.jpg` ainsi qu'un fichier `montreux.jpg.json` dans le bucket S3. Le fichier `montreux.jpg.json` doit contenir le résultat de l'analyse de l'image.

### Scénario 3

Dans ce scénario, le bucket existe et l'image `tour-de-peilz.jpg` y est déjà stockée.

Voici les différentes étapes:
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.

#### Résultat attendu
Nous devons obtenir un fichier `tour-de-peilz.jpg.json` dans le bucket S3. Ce fichier contient le résultat de l'analyse de l'image.
