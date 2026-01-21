# Etapa de construcción
FROM maven:3.8.5-openjdk-17 AS build
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM openjdk:17.0.1-jdk-slim
COPY --from=build /target/demo-0.0.1-SNAPSHOT.jar app.jar
# NOTA: Reemplaza 'demo-0.0.1-SNAPSHOT.jar' por el nombre real de tu jar en target/
# Puedes ver el nombre en tu pom.xml bajo <artifactId> y <version>
EXPOSE 9090
ENTRYPOINT ["java","-jar","app.jar"]