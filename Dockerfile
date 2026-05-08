# Stage 1: Builder - compile all modules
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy gradle wrapper and build scripts first for layer caching
COPY gradlew .
COPY gradle/ gradle/
COPY settings.gradle .
COPY build.gradle .

# Copy submodule build files
COPY tmk-core/build.gradle tmk-core/
COPY tmk-infra/build.gradle tmk-infra/
COPY tmk-api/build.gradle tmk-api/
COPY tmk-batch/build.gradle tmk-batch/

# Pre-fetch dependencies (cache layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy all source code
COPY tmk-core/src tmk-core/src
COPY tmk-infra/src tmk-infra/src
COPY tmk-api/src tmk-api/src
COPY tmk-batch/src tmk-batch/src

# Build both bootable jars, skip tests
RUN ./gradlew :tmk-api:bootJar :tmk-batch:bootJar --no-daemon -x test


# Stage 2: tmk-api runtime image
FROM eclipse-temurin:21-jre AS api

WORKDIR /app

COPY --from=builder /app/tmk-api/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]


# Stage 3: tmk-batch runtime image
FROM eclipse-temurin:21-jre AS batch

WORKDIR /app

COPY --from=builder /app/tmk-batch/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
