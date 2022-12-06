# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy as base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

FROM base as build
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-jammy as production
RUN groupadd -r user && useradd -r -g user user
USER user:user
EXPOSE 8080
COPY --from=build /app/target/data-object-service-*.jar /data-object-service.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/data-object-service.jar"]