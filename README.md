# Labelize

## Collaborateurs

### Product Owners

* **[Nicolas Glassey](https://github.com/NicolasGlassey)** : Enseignant pour le cours AMT à l'HEIG-VD


* **[Adrien Allemand](https://github.com/AdrienAllemand)** : Assistant pour le cours AMT à l'HEIG-VD

### Développeurs

* **[Jonathan Friedli](https://github.com/Marinlestylo)** : Etudiant en troisième année à l'HEIG-VD en ingénierie logicielle. Responsable du micro-service de labelisation d'images.


* **[Lazar Pavicevic](https://github.com/Lazzzer)** : Etudiant en troisième année à l'HEIG-VD en ingénierie logicielle. Responsable du micro-service de data-objects.

## Description

Ce projet est une application Java permettant de détecter des labels sur une image fournie. Il s'agit d'une application découpée en plusieurs micro-services. De plus, cette application est conçue afin d'être capable d'utiliser plusieurs providers clouds pour la reconnaissance d'images et le stockage des données.

Ce projet est la version microservice de [PictureLabelizer](https://github.com/AMT-TEAM07/PictureLabelizer).


## Providers cloud supportés

- [x] [AWS](https://aws.amazon.com/fr/) (🚧 développement en cours 🚧)
- [ ] [Azure](https://azure.microsoft.com/fr-fr/)
- [ ] [Google Cloud](https://cloud.google.com/?hl=fr)

## Structure du repository
Nous avons fait le choix de n'avoir qu'un seul repository pour l'ensemble du projet. Malgré le fait que nous ayons 3 projets distincts: `data-object-service`, `label-detector-service` et `cli-client`. Le `data-object-service` gère toute la partie de stockage d'objet, le `label-detector-service` gère la partie d'analyse d'images et le `cli-client` fait le lien entre les deux micro-services en utilisant leur API REST.

Nous avons adapté le CI/CD pour qu'il ne lance les tests uniquement sur la partie modifiée lors du dernier commit afin d'éviter de lancer les tests sur tous les projets à chaque commit.

## Wiki

Le [wiki](https://github.com/AMT-TEAM07/Labelize/wiki) du projet regroupe toutes les informations nécessaires pour comprendre notre méthodologie de travail, nos choix et la documentation utilisée pour implémenter notre projet.

## Docker
Dans repository, nous avons créé un fichier `docker-compose.yml` qui permet de lancer les deux micro-services. Pour cela, il faut se placer à la racine du dossier `docker-compose` et lancer la commande suivante:

```bash
docker-compose -f docker-compose.prod up
```

Cela lancera `data-object-service` et `label-detector-service` sur les ports `8080` et `8081` respectivement.

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

Les credentials doivent figurer dans le fichier `.env` à la racine des projets `data-object-service` et `label-detector-service`.

Pour `data-object-service`, il faut les variables d'environnement suivantes:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

Ensuite, après avoir créé un bucket S3, il faut rajouter son nom dans le fichier `.env` pour la variable suivante:

- `AWS_BUCKET`

Pour `label-detector-service`, il faut les variables d'environnement suivantes:

- `AWS_ACCESS_KEY_ID`
- `AWS_SECRET_ACCESS_KEY`
- `AWS_DEFAULT_REGION`

Chaque variable d'environnement a un équivalent avec un préfixe `TEST` qui est utilisé lors des tests locaux et dans la Github Action. Pour plus d'information, vous pouvez consulter les fichiers `.env.example` qui sont à la racines dedits projets.

## Mise en route
Afin de simplifier la mise en route des deux micro-services, nous avons créé un README dédié à chacun d'entre eux. Vous pouvez les consulter en cliquant sur les liens suivants:
* [Mise en route de la partie data-object-service](https://github.com/AMT-TEAM07/Labelize/tree/main/data-object-service/README.md)
* [Mise en route de la partie label-detection-service](https://github.com/AMT-TEAM07/Labelize/tree/main/data-object-service/README.md)