# syntax=docker/dockerfile:1

# ---- Estagio 1: build com Maven (cache das dependencias entre builds) ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
# Os testes rodam com "mvn test" (H2); aqui so empacotamos.
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

# ---- Estagio 2: apenas o JRE e o jar, com usuario sem privilegios ----
FROM eclipse-temurin:17-jre
RUN groupadd --system armazem && useradd --system --gid armazem --no-create-home armazem
WORKDIR /app
COPY --from=build /build/target/armazem.jar app.jar
USER armazem
ENV JAVA_TOOL_OPTIONS="-Duser.timezone=America/Sao_Paulo -XX:MaxRAMPercentage=75"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
