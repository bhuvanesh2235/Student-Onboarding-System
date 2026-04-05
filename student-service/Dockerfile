# ────────────────────────────────────────────────────────────────
# Dockerfile – student-service  (Spring Boot backend)
#
# Build context: ./student-service
# Two-stage build: Maven build → lightweight JRE runtime
# ────────────────────────────────────────────────────────────────

# ── Stage 1: Build the JAR ──────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper + pom first (layer cache for dependency downloads)
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies without building (cache layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build the fat JAR, skipping tests for speed
COPY src src
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Minimal JRE runtime ────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Spring datasource values are overridden by docker-compose environment vars
ENTRYPOINT ["java", "-jar", "app.jar"]
