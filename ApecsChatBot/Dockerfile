# 1. Etapa de construcción (Usamos una imagen con Java 21)
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución (Usamos una imagen ligera de Java 21)
FROM eclipse-temurin:21-jre
WORKDIR /app
# OJO AQUÍ: Asegúrate de que el nombre del .jar coincida con el que genera tu pom.xml
# Si tu artifactId es 'whatsapp-bot' y version '0.0.1-SNAPSHOT', esto debería funcionar:
COPY --from=build /app/target/whatsapp-bot-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]
