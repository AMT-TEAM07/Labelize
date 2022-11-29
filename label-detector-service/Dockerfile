# syntax=docker/dockerfile:1

FROM eclipse-temurin:17-jdk-jammy as base
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:resolve
COPY src ./src

FROM base as test
RUN ["./mvnw", "test"]

FROM base as development
CMD ["./mvnw", "spring-boot:run", "-Dspring-boot.run.jvmArguments='-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:8001'"]

FROM base as build
RUN ./mvnw package

FROM eclipse-temurin:17-jre-jammy as production
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
EXPOSE 8081
COPY --from=build /app/target/label-detector-service-*.jar /label-detector-service.jar
CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/label-detector-service.jar"]