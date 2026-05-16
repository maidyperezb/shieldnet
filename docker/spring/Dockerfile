FROM maven:3.9.5-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia el JAR ya construido en target/
COPY target/*.jar app.jar

ENV SERVER_PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
