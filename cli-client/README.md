# Label-detection-service

Bienvenue dans le README dédié au `cli-client`. Ce projet communique avec les deux autres micro-services de l'application: `label-detection-service` et `label-detection-service` au travers de leur API REST. 

Cette partie du  projet a pour but de faire des tests d'intégration entre les micro-services via les différents scénarios décrits plus bas.

## Prérequis

- [Java 17 (LTS)](https://adoptium.net/temurin/releases)
- [Maven 3.8](https://maven.apache.org/download.cgi)

Optionnel mais fortement recommandé:

- [IntelliJ IDEA](https://www.jetbrains.com/fr-fr/idea/download/#section=windows)

## Mise en route

1. Installer les dépendances
```bash
mvn clean install
```

## Scénarios
Nous avons 3 scénarios de tests donnés par le mandant du projet. Chaque scénario est décrit dans les sections suivantes.

### Scénario 1

Dans ce scénario, rien n'existe. Nous devons donc créer le bucket et y upload une image.

Voici les différentes étapes:
* Le bucket doit être créé
* L’image doit être uploadée
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.

### Scénario 2

Dans ce scénario, le bucket existe mais il est vide.

Voici les différentes étapes:
* L’image doit être uploadée
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.

### Scénario 3

Dans ce scénario, le bucket existe et l'image y est déjà stockée.

Voici les différentes étapes:
* Publication de l'image grâce à une URL signée
* Analyse de l'image
* Livraision du résultat sur le bucket hébergeant l’image.
