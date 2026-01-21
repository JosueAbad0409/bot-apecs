# 1. Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 2. Etapa de ejecución
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# NO necesitas EXPOSE en Render - usa PORT dinámico
ENTRYPOINT ["java","-jar","app.jar"]