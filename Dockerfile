# Usa un'immagine base con Java JDK e Maven
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Directory di lavoro
WORKDIR /app

# Copia il file pom.xml e scarica le dipendenze
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia il codice sorgente
COPY src ./src

# Compila l'applicazione con Maven
RUN mvn clean package -DskipTests

# Stage finale per l'esecuzione
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copia il JAR compilato dallo stage di build
COPY --from=build /app/target/*.jar app.jar

# Espone la porta per l'applicazione
EXPOSE 8080

# Comando per eseguire l'applicazione
CMD ["java", "-jar", "app.jar"]
