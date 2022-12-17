# syntax=docker/dockerfile:1

FROM maven:3.8.6-eclipse-temurin-17 as base
WORKDIR /app
COPY pom.xml ./
COPY src ./src

FROM base as build
RUN mvn package -DskipTests

FROM eclipse-temurin:17-jre-jammy as production
COPY lausanne.jpg ./lausanne.jpg
COPY montreux.jpg ./montreux.jpg
COPY tour-de-peilz.jpg ./tour-de-peilz.jpg
COPY --from=build /app/target/cli-client-*.jar /cli-client.jar
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/cli-client.jar"]