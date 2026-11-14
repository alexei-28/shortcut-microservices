# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Сборка без запуска тестов для скорости (или с тестами, если требуется)
RUN ./gradlew :post-service:bootJar -x test

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/post-service/build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]