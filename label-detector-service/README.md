# Labelize : label-detector-service

[![Verify & Package - Label Detector Service](https://github.com/AMT-TEAM07/Labelize/actions/workflows/label-detector-verify.yml/badge.svg)](https://github.com/AMT-TEAM07/Labelize/actions/workflows/label-detector-verify.yml) [![Deploy on Docker Hub - Label Detector Service](https://github.com/AMT-TEAM07/Labelize/actions/workflows/label-detector-deploy.yml/badge.svg)](https://github.com/AMT-TEAM07/Labelize/actions/workflows/label-detector-deploy.yml)

Ce micro-service fait parti du projet [Labelize](https://github.com/AMT-TEAM07/Labelize).

Il s'agit d'une application Spring Boot mettant à disposition une API REST.

Ce micro-service est chargé de l'analyse d'image. Il permet de détecter les labels présents sur une image. Il s'appuie sur le service AWS Rekognition pour effectuer cette analyse.

## Prérequis

- [Java 17 (LTS)](https://adoptium.net/temurin/releases)
- [Maven 3.8](https://maven.apache.org/download.cgi)

Optionnel mais fortement recommandé:

- [IntelliJ IDEA](https://www.jetbrains.com/fr-fr/idea/download/#section=windows)

### Pour AWS

#### Outils à installer :

- AWS CLI
  - [AWS CLI Installation](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
  - [AWS CLI Configuration](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-quickstart.html)

Optionnel mais fortement recommandé:

- [AWS Toolkit](https://docs.aws.amazon.com/toolkit-for-jetbrains/latest/userguide/welcome.html)

### Credentials

Les credentials doivent figurer dans le fichier `.env` à la racine du dossier `label-detector-service`. Voici les différentes variables d'environnement qu'il faut renseigner:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

Chaque variable d'environnement a un équivalent avec un préfixe `TEST` qui est utilisé lors des tests locaux et dans la Github Action. Pour plus d'information, vous pouvez consulter le fichier `.env.example` qui est également à la racine du dossier `label-detector-service`.

## Mise en route

1. Créer le fichier .env avec lesdites variables d'environnement.

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

3. Lancer les tests unitaires

```bash
# Pour lancer tous les tests
mvn test

# Pour lancer une classe de test spécifique
mvn test -Dtest=NomDeLaClasseTest
```

4. Créer un exécutable JAR et lancer l'application

```bash
# Package l'application
mvn package -DskipTests

# Lancer l'application
java -jar target/*.jar
```

## Endpoints

Les endpoints acceptent et retournent du `JSON`. Il faut donc spécifier le header `Content-Type: application/json` dans les requêtes.

Voici les endpoints actuellement disponibles:

### Analyse d'une image dont l'url est passée dans la requête

```bash
# URI
[POST] /v1/label-detector-management/analyze/url

# Exemple de body JSON de la requête
{
    "image": "https://static.nationalgeographic.co.uk/files/styles/image_3200/public/ngts_web_st_insideguide_lausanne_14_hr.jpg",
    "maxLabels": 50,
    "minConfidence": 55.7
}
```

Les attributs `maxLabels` et `minConfidence` sont optionnels. S'ils ne sont pas renseignés, ils prendront les valeurs par défaut suivantes:

- maxLabels: 10
- minConfidence: 90.0

### Analyse d'une image passée en base 64 dans la requête

```bash
# URI
[POST] /v1/label-detector-management/analyze/url

# Exemple de body JSON de la requête
{
    "image": "<imageEnBase64>",
    "maxLabels": 50,
    "minConfidence": 55.7
}
```

Les attributs `maxLabels` et `minConfidence` sont optionnels. S'ils ne sont pas renseignés, ils prendront les valeurs par défaut suivantes:

- maxLabels: 10
- minConfidence: 90.0

### Réponses de l'API

Les 3 codes d'erreurs possibles:

- 200: L'analyse a été effectuée avec succès
- 400: La requête est invalide (soit car il manque l'attribue image dans le body de la requête, soit car l'attribut image est invalid car l'url n'est pas correcte).
- 404: L'URL demandée n'existe pas
