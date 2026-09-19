# ---- Etapa de build: compila el jar con Maven ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Cachear dependencias en su propia capa (solo se re-descargan si pom.xml cambia)
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests

# ---- Etapa de runtime: solo el JRE + el jar ya construido ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S vidasalud && adduser -S vidasalud -G vidasalud
COPY --from=build /app/target/*.jar app.jar
USER vidasalud

EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
