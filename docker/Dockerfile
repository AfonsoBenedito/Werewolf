# Stage 1: Build frontend
FROM node:20-alpine AS frontend
WORKDIR /app/web
COPY web/package.json web/package-lock.json ./
RUN npm ci
COPY web/ ./
RUN npm run build

# Stage 2: Build backend (with frontend assets bundled in)
FROM gradle:8-jdk17 AS backend
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/ gradle/
COPY src/ src/
COPY detekt.yml ./
COPY --from=frontend /app/web/dist/ src/main/resources/static/
RUN ./gradlew bootJar --no-daemon

# Stage 3: Runtime
FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y --no-install-recommends redis-server && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=backend /app/build/libs/*.jar app.jar
COPY docker/start.sh .
RUN chmod +x start.sh
EXPOSE 8080
ENTRYPOINT ["./start.sh"]
