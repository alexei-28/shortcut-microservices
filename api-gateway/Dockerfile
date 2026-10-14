# In root folder run
# docker build -f api-gateway/Dockerfile -t api-gateway:latest .

# --- Stage 1: Build the application ---
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app

# Copy root build configurations and wrapper
COPY gradlew .
COPY gradle gradle
COPY settings.gradle.kts .
COPY api-gateway/build.gradle.kts api-gateway/build.gradle.kts

# Pre-download dependencies for caching
WORKDIR /app/api-gateway
RUN ../gradlew dependencies --no-daemon

# Copy source code and build fat-JAR
WORKDIR /app
COPY api-gateway/src api-gateway/src

WORKDIR /app/api-gateway
RUN ../gradlew bootJar -x test --no-daemon


# --- Stage 2: Lightweight production runtime ---
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# Create a non-root system user for security
RUN groupadd -g 1001 spring && useradd -u 1001 -g spring -m spring
USER spring:spring

# Copy the compiled artifact from the builder stage
COPY --from=builder /app/api-gateway/build/libs/*.jar app.jar

# Production JVM options for containerized environments
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]