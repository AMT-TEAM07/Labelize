# Label-detection-service

Bienvenue dans le README dédié au `label-detection-service`. Ce micro-service est en charge de l'analyse d'image. Il permet de détecter les labels présents sur une image.
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

Les credentials doivent figurer dans le fichier `.env` à la racine du dossier `label-detection-service`. Voici les différentes variables d'environnement qu'il faut renseigner:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

Chaque variable d'environnement a un équivalent avec un préfixe `TEST` qui est utilisé lors des tests locaux et dans la Github Action. Pour plus d'information, vous pouvez consulter le fichier `.env.example` qui est également à la racine du dossier `label-detection-service`.

## Endpoints
Voici les endpoints actuellement disponibles:

## Analyse d'une image dont l'url est passée dans la requête
```bash
# URI
HTTP POST http://nomdomaine/v1/label-detector-management/analyze/url

# Body de la requête
{
    "image": "https://static.nationalgeographic.co.uk/files/styles/image_3200/public/ngts_web_st_insideguide_lausanne_14_hr.jpg",
    "maxLabels": 50,
    "minConfidence": 55.7
}
```
Les attributs maxLabels et minConfidence sont optionnels. S'ils ne sont pas renseignés, ils prendront les valeurs par défaut suivantes: 
- maxLabels: 10
- minConfidence: 90.0

## Analyse d'une image passée en base 64 dans la requête
```bash
# URI
HTTP POST http://nomdomaine/v1/label-detector-management/analyze/url

# Body de la requête
{
    "image": "<imageEnBase64>",
    "maxLabels": 50,
    "minConfidence": 55.7
}
```
Les attributs maxLabels et minConfidence sont optionnels. S'ils ne sont pas renseignés, ils prendront les valeurs par défaut suivantes: 
- maxLabels: 10
- minConfidence: 90.0

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
mvn clean install -dskipTests
```

3. Lancer les tests unitaires
```bash
# Pour lancer tous les tests
mvn test

# Pour lancer un test spécifique
mvn test -Dtest=NomDeLaClasseTest
```