# Data-object-service

Bienvenue dans le README dédié au `data-object-service`. Ce micro-service est en charge de la gestion du stockage. Il permet de stocker des objets dans un bucket S3 et de les récupérer.

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
mvn clean install -dskipTests
```

3. Lancer les tests unitaires
```bash
# Pour lancer tous les tests
mvn test

# Pour lancer un test spécifique
mvn test -Dtest=NomDeLaClasseTest
```