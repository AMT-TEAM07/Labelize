# Data-object-service

Bienvenue dans le README dédié au `data-object-service`. Ce micro-service est chargé de la gestion du stockage. Il permet de stocker des objets dans un bucket S3 et de les récupérer avec des urls temporaires.

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

Les credentials doivent figurer dans le fichier `.env` à la racine du dossier `data-object-service`. Voici les différentes variables d'environnement qu'il faut renseigner:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

Ensuite, après avoir créé un bucket S3, il faut rajouter son nom dans le fichier `.env` pour la variable suivante:

- `AWS_BUCKET`

Chaque variable d'environnement a un équivalent avec un préfixe `TEST` qui est utilisé lors des tests locaux et dans la Github Action. Pour plus d'information, vous pouvez consulter le fichier `.env.example` qui est également à la racine du dossier `data-object-service`.

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

La majorité des endpoints acceptent et retournent du `JSON`. Il faut donc spécifier le header `Content-Type: application/json` dans les requêtes.

Voici les endpoints actuellement disponibles:

### Upload d'un data-objet dans le bucket S3

```bash
# URI
[POST] v1/data-object-management/data-objects

# Exemple avec curl, envoyant un fichier dans un form-data
curl -X POST \
  http://localhost:8080/v1/data-object-management/data-objects \
  -H 'Content-Type: multipart/form-data' \
  -F file=@CHEMIN_VERS_LE_FICHIER
```

> **Note**
> Il est impératif de spécifier le header `Content-Type: multipart/form-data` et d'envoyer le fichier avec un paramètre `file` dans le body pour cette requête.

### Récupérer l'url d'un data-objet dans le bucket S3

```bash
# URI
[GET] v1/data-object-management/data-objects

# Exemple de requête avec des Requests Params
{
    "objectName" : "test-image.png",
    "expiration" : 10
}

# Cela donne l'URI suivante:
v1/data-object-management/data-objects?objectName=test-image.png&expiration=10
```

L'attribut ´expiration´ est optionnel. S'il n'est pas renseigné, il prend la valeur par défaut de `90` secondes.

### Suppression d'un data-object sur le bucket S3

```bash
# URI
[DELETE] v1/data-object-management/data-objects

# Exemple de requête avec des Requests Params
{
    "isRootObject" : false,
    "objectName" : "test-image.png",
    "recursive" : false
}

# Cela donne l'URI suivante:
v1/data-object-management/data-objects?isRootObject=false&objectName=test-image.png&recursive=false
```

L'attribut ´isRootObject´ permet de spécifier si l'objet à supprimer est un bucket ou non. L'attribut ´recursive´ permet de spécifier de confirmer la suppression récursive d'un dossier ou d'un bucket non vide.
